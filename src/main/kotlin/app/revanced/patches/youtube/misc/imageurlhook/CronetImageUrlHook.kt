package app.revanced.patches.youtube.misc.imageurlhook

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.youtube.misc.imageurlhook.fingerprints.MessageDigestImageUrlFingerprint
import app.revanced.patches.youtube.misc.imageurlhook.fingerprints.MessageDigestImageUrlParentFingerprint
import app.revanced.patches.youtube.misc.imageurlhook.fingerprints.cronet.RequestFingerprint
import app.revanced.patches.youtube.misc.imageurlhook.fingerprints.cronet.request.callback.OnFailureFingerprint
import app.revanced.patches.youtube.misc.imageurlhook.fingerprints.cronet.request.callback.OnResponseStartedFingerprint
import app.revanced.patches.youtube.misc.imageurlhook.fingerprints.cronet.request.callback.OnSucceededFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

@Patch(
    description = "Hooks Cronet image urls",
    dependencies = [
        IntegrationsPatch::class
    ]
)
object CronetImageUrlHook : BytecodePatch(
    setOf(
        MessageDigestImageUrlParentFingerprint,
        OnResponseStartedFingerprint,
        RequestFingerprint
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
    fun addImageUrlHook(targetMethodClass: String, highPriority: Boolean = false) {
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
    fun addImageUrlSuccessCallbackHook(targetMethodClass: String) {
        loadImageSuccessCallbackMethod.addInstruction(
            loadImageSuccessCallbackIndex++,
            "invoke-static { p1, p2 }, $targetMethodClass->handleCronetSuccess(" +
                "Lorg/chromium/net/UrlRequest;Lorg/chromium/net/UrlResponseInfo;)V",
        )
    }

    /**
     * If a connection outright failed to complete any connection.
     */
    fun addImageUrlErrorCallbackHook(targetMethodClass: String) {
        loadImageErrorCallbackMethod.addInstruction(
            loadImageErrorCallbackIndex++,
            "invoke-static { p1, p2, p3 }, $targetMethodClass->handleCronetFailure(" +
                "Lorg/chromium/net/UrlRequest;Lorg/chromium/net/UrlResponseInfo;Ljava/io/IOException;)V",
        )
    }

    override fun execute(context: BytecodeContext) {
        fun MethodFingerprint.alsoResolve(fingerprint: MethodFingerprint) =
            also { resolve(context, fingerprint.resultOrThrow().classDef) }.resultOrThrow()

        loadImageUrlMethod = MessageDigestImageUrlFingerprint
            .alsoResolve(MessageDigestImageUrlParentFingerprint).mutableMethod

        loadImageSuccessCallbackMethod = OnSucceededFingerprint
            .alsoResolve(OnResponseStartedFingerprint).mutableMethod

        loadImageErrorCallbackMethod = OnFailureFingerprint
            .alsoResolve(OnResponseStartedFingerprint).mutableMethod

        // The URL is required for the failure callback hook, but the URL field is obfuscated.
        // Add a helper get method that returns the URL field.
        RequestFingerprint.resultOrThrow().apply {
            // The url is the only string field that is set inside the constructor.
            val urlFieldInstruction = mutableMethod.getInstructions().single {
                if (it.opcode != Opcode.IPUT_OBJECT) return@single false

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
                    MutableMethodImplementation(2),
                ).toMutable().apply {
                    addInstructions(
                        """
                        iget-object v0, p0, $definingClass->$urlFieldName:Ljava/lang/String;
                        return-object v0
                    """,
                    )
                }
            )
        }
    }
}
