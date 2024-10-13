package app.revanced.patches.youtube.misc.imageurlhook

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.util.applyMatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

private lateinit var loadImageUrlMethod: MutableMethod
private var loadImageUrlIndex = 0

private lateinit var loadImageSuccessCallbackMethod: MutableMethod
private var loadImageSuccessCallbackIndex = 0

private lateinit var loadImageErrorCallbackMethod: MutableMethod
private var loadImageErrorCallbackIndex = 0

val cronetImageUrlHookPatch = bytecodePatch(
    description = "Hooks Cronet image urls.",
) {
    dependsOn(sharedExtensionPatch)

    val messageDigestImageUrlParentMatch by messageDigestImageUrlParentFingerprint()
    val onResponseStartedMatch by onResponseStartedFingerprint()
    val requestMatch by requestFingerprint()

    execute { context ->
        loadImageUrlMethod = messageDigestImageUrlFingerprint
            .applyMatch(context, messageDigestImageUrlParentMatch).mutableMethod

        loadImageSuccessCallbackMethod = onSucceededFingerprint
            .applyMatch(context, onResponseStartedMatch).mutableMethod

        loadImageErrorCallbackMethod = onFailureFingerprint
            .applyMatch(context, onResponseStartedMatch).mutableMethod

        // The URL is required for the failure callback hook, but the URL field is obfuscated.
        // Add a helper get method that returns the URL field.
        // The url is the only string field that is set inside the constructor.
        val urlFieldInstruction = requestMatch.mutableMethod.instructions.single {
            if (it.opcode != Opcode.IPUT_OBJECT) return@single false

            val reference = (it as ReferenceInstruction).reference as FieldReference
            reference.type == "Ljava/lang/String;"
        } as ReferenceInstruction

        val urlFieldName = (urlFieldInstruction.reference as FieldReference).name
        val definingClass = CRONET_URL_REQUEST_CLASS_DESCRIPTOR
        val addedMethodName = "getHookedUrl"
        requestMatch.mutableClass.methods.add(
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
