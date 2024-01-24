package app.revanced.patches.youtube.layout.thumbnails

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.shared.settings.preference.impl.*
import app.revanced.patches.youtube.layout.thumbnails.fingerprints.MessageDigestImageUrlFingerprint
import app.revanced.patches.youtube.layout.thumbnails.fingerprints.MessageDigestImageUrlParentFingerprint
import app.revanced.patches.youtube.layout.thumbnails.fingerprints.cronet.RequestFingerprint
import app.revanced.patches.youtube.layout.thumbnails.fingerprints.cronet.request.callback.OnFailureFingerprint
import app.revanced.patches.youtube.layout.thumbnails.fingerprints.cronet.request.callback.OnResponseStartedFingerprint
import app.revanced.patches.youtube.layout.thumbnails.fingerprints.cronet.request.callback.OnSucceededFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

@Patch(
    name = "Alternative thumbnails",
    description = "Adds options to replace video thumbnails using the DeArrow API or image captures from the video.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class, AlternativeThumbnailsResourcePatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
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
                "19.03.35"
            ]
        )
    ]
)
@Suppress("unused")
object AlternativeThumbnailsPatch : BytecodePatch(
    setOf(
        MessageDigestImageUrlParentFingerprint,
        OnResponseStartedFingerprint,
        RequestFingerprint,
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/AlternativeThumbnailsPatch;"

    private lateinit var loadImageUrlMethod: MutableMethod
    private var loadImageUrlIndex = 0

    private lateinit var loadImageSuccessCallbackMethod: MutableMethod
    private var loadImageSuccessCallbackIndex = 0

    private lateinit var loadImageErrorCallbackMethod: MutableMethod
    private var loadImageErrorCallbackIndex = 0

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
                """
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
                    "Lorg/chromium/net/UrlRequest;Lorg/chromium/net/UrlResponseInfo;)V"
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
                    "Lorg/chromium/net/UrlRequest;Lorg/chromium/net/UrlResponseInfo;Ljava/io/IOException;)V"
        )
    }

    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                "revanced_alt_thumbnail_preference_screen",
                StringResource("revanced_alt_thumbnail_preference_screen_title", "Alternative thumbnails"),
                listOf(
                    NonInteractivePreference(
                        StringResource("revanced_alt_thumbnail_about_title", "Thumbnails in use"),
                        null, // Summary is dynamically updated based on the current settings.
                        tag = "app.revanced.integrations.youtube.settings.preference.AlternativeThumbnailsStatusPreference"
                    ),
                    SwitchPreference(
                        "revanced_alt_thumbnail_dearrow",
                        StringResource("revanced_alt_thumbnail_dearrow_title", "Enable DeArrow thumbnails"),
                        StringResource("revanced_alt_thumbnail_dearrow_summary_on", "Using DeArrow thumbnails"),
                        StringResource("revanced_alt_thumbnail_dearrow_summary_off", "Not using DeArrow thumbnails")
                    ),
                    SwitchPreference(
                        "revanced_alt_thumbnail_dearrow_connection_toast",
                        StringResource("revanced_alt_thumbnail_dearrow_connection_toast_title", "Show a toast if API is not available"),
                        StringResource("revanced_alt_thumbnail_dearrow_connection_toast_summary_on", "Toast is shown if DeArrow is not available"),
                        StringResource("revanced_alt_thumbnail_dearrow_connection_toast_summary_off", "Toast is not shown if DeArrow is not available")
                    ),
                    TextPreference(
                        "revanced_alt_thumbnail_dearrow_api_url",
                        StringResource(
                            "revanced_alt_thumbnail_dearrow_api_url_title",
                            "DeArrow API endpoint"
                        ),
                        StringResource(
                            "revanced_alt_thumbnail_dearrow_api_url_summary",
                            "The URL of the DeArrow thumbnail cache endpoint. " +
                                    "Do not change this unless you know what you\\\'re doing"
                        ),
                    ),
                    NonInteractivePreference(
                        StringResource(
                            "revanced_alt_thumbnail_dearrow_about_title",
                            "About DeArrow"
                        ),
                        StringResource(
                            "revanced_alt_thumbnail_dearrow_about_summary",
                            "DeArrow provides crowd-sourced thumbnails for YouTube videos. " +
                                    "These thumbnails are often more relevant than those provided by YouTube. " +
                                    "If enabled, video URLs will be sent to the API server and no other data is sent"
                                    + "\\n\\nTap here to learn more about DeArrow"
                        ),
                        // Custom about preference with link to the DeArrow website.
                        tag = "app.revanced.integrations.youtube.settings.preference.AlternativeThumbnailsAboutDeArrowPreference",
                        selectable = true
                    ),
                    SwitchPreference(
                        "revanced_alt_thumbnail_stills",
                        StringResource("revanced_alt_thumbnail_stills_title", "Enable still video captures"),
                        StringResource("revanced_alt_thumbnail_stills_summary_on", "Using YouTube still video captures"),
                        StringResource("revanced_alt_thumbnail_stills_summary_off", "Not using YouTube still video captures")
                    ),
                    ListPreference(
                        "revanced_alt_thumbnail_stills_time",
                        StringResource("revanced_alt_thumbnail_stills_time_title", "Video time to take the still from"),
                        ArrayResource(
                            "revanced_alt_thumbnail_type_entries",
                            listOf(
                                StringResource("revanced_alt_thumbnail_stills_time_entry_1", "Beginning of video"),
                                StringResource("revanced_alt_thumbnail_stills_time_entry_2", "Middle of video"),
                                StringResource("revanced_alt_thumbnail_stills_time_entry_3", "End of video"),
                            )
                        ),
                        ArrayResource(
                            "revanced_alt_thumbnail_stills_time_entry_values",
                            listOf(
                                StringResource("revanced_alt_thumbnail_stills_time_entry_value_1", "1"),
                                StringResource("revanced_alt_thumbnail_stills_time_entry_value_2", "2"),
                                StringResource("revanced_alt_thumbnail_stills_time_entry_value_3", "3"),
                            )
                        )
                    ),
                    SwitchPreference(
                        "revanced_alt_thumbnail_stills_fast",
                        StringResource(
                            "revanced_alt_thumbnail_stills_fast_title",
                            "Use fast still captures"
                        ),
                        StringResource(
                            "revanced_alt_thumbnail_stills_fast_summary_on",
                            "Using medium quality still captures. " +
                                    "Thumbnails will load faster, but live streams, unreleased, " +
                                    "or very old videos may show blank thumbnails"
                        ),
                        StringResource(
                            "revanced_alt_thumbnail_stills_fast_summary_off",
                            "Using high quality still captures"
                        )
                    ),
                    NonInteractivePreference(
                        StringResource(
                            "revanced_alt_thumbnail_stills_about_title",
                            "About still video captures"
                        ),
                        StringResource(
                            "revanced_alt_thumbnail_stills_about_summary",
                            "Still captures are taken from the beginning/middle/end of each video. " +
                                    "These images are built into YouTube and no external API is used"
                        ),
                        // Restore the preference dividers to keep it from looking weird.
                        selectable = true
                    )
                ),
                StringResource("revanced_alt_thumbnail_preference_screen_summary", "Video thumbnail settings")
            )
        )

        fun MethodFingerprint.getResultOrThrow() =
            result ?: throw exception

        fun MethodFingerprint.alsoResolve(fingerprint: MethodFingerprint) =
            also { resolve(context, fingerprint.getResultOrThrow().classDef) }.getResultOrThrow()

        fun MethodFingerprint.resolveAndLetMutableMethod(
            fingerprint: MethodFingerprint,
            block: (MutableMethod) -> Unit
        ) = alsoResolve(fingerprint).also { block(it.mutableMethod) }

        MessageDigestImageUrlFingerprint.resolveAndLetMutableMethod(MessageDigestImageUrlParentFingerprint) {
            loadImageUrlMethod = it
            addImageUrlHook(INTEGRATIONS_CLASS_DESCRIPTOR, true)
        }

        OnSucceededFingerprint.resolveAndLetMutableMethod(OnResponseStartedFingerprint) {
            loadImageSuccessCallbackMethod = it
            addImageUrlSuccessCallbackHook(INTEGRATIONS_CLASS_DESCRIPTOR)
        }

        OnFailureFingerprint.resolveAndLetMutableMethod(OnResponseStartedFingerprint) {
            loadImageErrorCallbackMethod = it
            addImageUrlErrorCallbackHook(INTEGRATIONS_CLASS_DESCRIPTOR)
        }

        // The URL is required for the failure callback hook, but the URL field is obfuscated.
        // Add a helper get method that returns the URL field.
        RequestFingerprint.getResultOrThrow().apply {
            // The url is the only string field that is set inside the constructor.
            val urlFieldInstruction = mutableMethod.getInstructions().first {
                if (it.opcode != Opcode.IPUT_OBJECT) return@first false

                val reference = (it as ReferenceInstruction).reference as FieldReference
                reference.type == "Ljava/lang/String;"
            } as ReferenceInstruction

            val urlFieldName = (urlFieldInstruction.reference as FieldReference).name
            val definingClass = RequestFingerprint.IMPLEMENTATION_CLASS_NAME
            val addedMethodName = "getHookedUrl"
            mutableClass.methods.add(
                ImmutableMethod(
                    definingClass,
                    addedMethodName,
                    emptyList(),
                    "Ljava/lang/String;",
                    AccessFlags.PUBLIC.value,
                    null,
                    null,
                    MutableMethodImplementation(2)
                ).toMutable().apply {
                    addInstructions(
                        """
                        iget-object v0, p0, $definingClass->${urlFieldName}:Ljava/lang/String;
                        return-object v0
                    """
                    )
                })
        }
    }
}
