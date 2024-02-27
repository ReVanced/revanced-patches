package app.revanced.patches.youtube.alternativethumbnails.general

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
import app.revanced.patches.youtube.alternativethumbnails.general.fingerprints.MessageDigestImageUrlFingerprint
import app.revanced.patches.youtube.alternativethumbnails.general.fingerprints.MessageDigestImageUrlParentFingerprint
import app.revanced.patches.youtube.alternativethumbnails.general.fingerprints.cronet.RequestFingerprint
import app.revanced.patches.youtube.alternativethumbnails.general.fingerprints.cronet.request.callback.OnFailureFingerprint
import app.revanced.patches.youtube.alternativethumbnails.general.fingerprints.cronet.request.callback.OnResponseStartedFingerprint
import app.revanced.patches.youtube.alternativethumbnails.general.fingerprints.cronet.request.callback.OnSucceededFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.ALTERNATIVE_THUMBNAILS
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch.contexts
import app.revanced.util.copyXmlNode
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
    dependencies = [SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
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
            addImageUrlHook(ALTERNATIVE_THUMBNAILS, true)
        }

        OnSucceededFingerprint.resolveAndLetMutableMethod(OnResponseStartedFingerprint) {
            loadImageSuccessCallbackMethod = it
            addImageUrlSuccessCallbackHook(ALTERNATIVE_THUMBNAILS)
        }

        OnFailureFingerprint.resolveAndLetMutableMethod(OnResponseStartedFingerprint) {
            loadImageErrorCallbackMethod = it
            addImageUrlErrorCallbackHook(ALTERNATIVE_THUMBNAILS)
        }

        // The URL is required for the failure callback hook, but the URL field is obfuscated.
        // Add a helper get method that returns the URL field.
        RequestFingerprint.getResultOrThrow().apply {
            // The url is the only string field that is set inside the constructor.
            val urlFieldInstruction = mutableMethod.getInstructions().first {
                if (it.opcode != Opcode.IPUT_OBJECT)
                    return@first false

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
                }
            )
        }

        /**
         * Copy arrays
         */
        contexts.copyXmlNode("youtube/alternativethumbnails/host", "values/arrays.xml", "resources")

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: ALTERNATIVE_THUMBNAILS_SETTINGS"
            )
        )

        SettingsPatch.updatePatchStatus("Alternative thumbnails")
    }
}
