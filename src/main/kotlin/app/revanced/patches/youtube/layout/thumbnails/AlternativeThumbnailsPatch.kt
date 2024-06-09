package app.revanced.patches.youtube.layout.thumbnails

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.NonInteractivePreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.layout.thumbnails.fingerprints.*
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.navigation.navigationBarHookPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

private const val INTEGRATIONS_CLASS_DESCRIPTOR =
    "Lapp/revanced/integrations/youtube/patches/AlternativeThumbnailsPatch;"

private lateinit var loadImageUrlMethod: MutableMethod
private var loadImageUrlIndex = 0

private lateinit var loadImageSuccessCallbackMethod: MutableMethod
private var loadImageSuccessCallbackIndex = 0

private lateinit var loadImageErrorCallbackMethod: MutableMethod
private var loadImageErrorCallbackIndex = 0

@Suppress("unused")
val alternativeThumbnailsPatch = bytecodePatch(
    name = "Alternative thumbnails",
    description = "Adds options to replace video thumbnails using the DeArrow API or image captures from the video.",
) {
    dependsOn(
        integrationsPatch,
        settingsPatch,
        addResourcesPatch,
        navigationBarHookPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.32.39",
            "18.37.36",
            "18.38.44",
            "18.43.45",
            "18.44.41",
            "18.45.43",
            "18.48.39",
            "18.49.37",
            "19.01.34",
            "19.02.39",
            "19.03.36",
            "19.04.38",
            "19.05.36",
            "19.06.39",
            "19.07.40",
            "19.08.36",
            "19.09.38",
            "19.10.39",
            "19.11.43",
        ),
    )

    val messageDigestImageUrlParentResult by messageDigestImageUrlParentFingerprint
    val onResponseStartedResult by onResponseStartedFingerprint
    val requestResult by requestFingerprint

    execute { context ->
        addResources("youtube", "layout.thumbnails.AlternativeThumbnailsPatch")

        val entries = "revanced_alt_thumbnail_options_entries"
        val values = "revanced_alt_thumbnail_options_entry_values"
        PreferenceScreen.ALTERNATIVE_THUMBNAILS.addPreferences(
            ListPreference(
                "revanced_alt_thumbnail_home",
                summaryKey = null,
                entriesKey = entries,
                entryValuesKey = values,
            ),
            ListPreference(
                "revanced_alt_thumbnail_subscription",
                summaryKey = null,
                entriesKey = entries,
                entryValuesKey = values,
            ),
            ListPreference(
                "revanced_alt_thumbnail_library",
                summaryKey = null,
                entriesKey = entries,
                entryValuesKey = values,
            ),
            ListPreference(
                "revanced_alt_thumbnail_player",
                summaryKey = null,
                entriesKey = entries,
                entryValuesKey = values,
            ),
            ListPreference(
                "revanced_alt_thumbnail_search",
                summaryKey = null,
                entriesKey = entries,
                entryValuesKey = values,
            ),
            NonInteractivePreference(
                "revanced_alt_thumbnail_dearrow_about",
                // Custom about preference with link to the DeArrow website.
                tag = "app.revanced.integrations.youtube.settings.preference.AlternativeThumbnailsAboutDeArrowPreference",
                selectable = true,
            ),
            SwitchPreference("revanced_alt_thumbnail_dearrow_connection_toast"),
            TextPreference("revanced_alt_thumbnail_dearrow_api_url"),
            NonInteractivePreference("revanced_alt_thumbnail_stills_about"),
            SwitchPreference("revanced_alt_thumbnail_stills_fast"),
            ListPreference("revanced_alt_thumbnail_stills_time", summaryKey = null),
        )

        fun MethodFingerprint.alsoResolve(result: MethodFingerprintResult) = also {
            resolve(context, result.classDef)
        }.resultOrThrow()

        fun MethodFingerprint.resolveAndLetMutableMethod(
            result: MethodFingerprintResult,
            block: (MutableMethod) -> Unit,
        ) = alsoResolve(result).also { block(it.mutableMethod) }

        messageDigestImageUrlFingerprint.resolveAndLetMutableMethod(messageDigestImageUrlParentResult) {
            loadImageUrlMethod = it
            addImageUrlHook(INTEGRATIONS_CLASS_DESCRIPTOR, true)
        }

        onSucceededFingerprint.resolveAndLetMutableMethod(onResponseStartedResult) {
            loadImageSuccessCallbackMethod = it
            addImageUrlSuccessCallbackHook(INTEGRATIONS_CLASS_DESCRIPTOR)
        }

        onFailureFingerprint.resolveAndLetMutableMethod(onResponseStartedResult) {
            loadImageErrorCallbackMethod = it
            addImageUrlErrorCallbackHook(INTEGRATIONS_CLASS_DESCRIPTOR)
        }

        // The URL is required for the failure callback hook, but the URL field is obfuscated.
        // Add a helper get method that returns the URL field.
        // The url is the only string field that is set inside the constructor.
        val urlFieldInstruction = requestResult.mutableMethod.instructions.first {
            if (it.opcode != Opcode.IPUT_OBJECT) return@first false

            val reference = (it as ReferenceInstruction).reference as FieldReference
            reference.type == "Ljava/lang/String;"
        } as ReferenceInstruction

        val urlFieldName = (urlFieldInstruction.reference as FieldReference).name
        val definingClass = CRONET_URL_REQUEST_CLASS_DESCRIPTOR
        val addedMethodName = "getHookedUrl"
        requestResult.mutableClass.methods.add(
            ImmutableMethod(
                definingClass,
                addedMethodName,
                emptyList(),
                "Ljava/lang/String;",
                AccessFlags.PUBLIC.value,
                null,
                null,
                MutableMethodImplementation(2),
            ).toMutable().apply {
                addInstructions(
                    """
                        iget-object v0, p0, $definingClass->$urlFieldName:Ljava/lang/String;
                        return-object v0
                    """,
                )
            },
        )
    }
}

/**
 * @param highPriority If the hook should be called before all other hooks.
 */
@Suppress("SameParameterValue")
private fun addImageUrlHook(targetMethodClass: String, highPriority: Boolean) {
    loadImageUrlMethod.addInstructions(
        if (highPriority) 0 else loadImageUrlIndex,
        """
                invoke-static { p1 }, $targetMethodClass->overrideImageURL(Ljava/lang/String;)Ljava/lang/String;
                move-result-object p1
                """,
    )
    loadImageUrlIndex += 2
}

/**
 * If a connection completed, which includes normal 200 responses but also includes
 * status 404 and other error like http responses.
 */
@Suppress("SameParameterValue")
private fun addImageUrlSuccessCallbackHook(targetMethodClass: String) {
    loadImageSuccessCallbackMethod.addInstruction(
        loadImageSuccessCallbackIndex++,
        "invoke-static { p1, p2 }, $targetMethodClass->handleCronetSuccess(" +
                "Lorg/chromium/net/UrlRequest;Lorg/chromium/net/UrlResponseInfo;)V",
    )
}

/**
 * If a connection outright failed to complete any connection.
 */
@Suppress("SameParameterValue")
private fun addImageUrlErrorCallbackHook(targetMethodClass: String) {
    loadImageErrorCallbackMethod.addInstruction(
        loadImageErrorCallbackIndex++,
        "invoke-static { p1, p2, p3 }, $targetMethodClass->handleCronetFailure(" +
                "Lorg/chromium/net/UrlRequest;Lorg/chromium/net/UrlResponseInfo;Ljava/io/IOException;)V",
    )
}
