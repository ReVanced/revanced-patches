package app.revanced.patches.ad

import app.revanced.patcher.PatcherData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.*
import app.revanced.patcher.signature.MethodMetadata
import app.revanced.patcher.signature.MethodSignature
import app.revanced.patcher.signature.MethodSignatureMetadata
import app.revanced.patcher.signature.PatternScanMethod
import app.revanced.patcher.smali.toInstructions
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

private val compatiblePackages = listOf("com.google.android.youtube")

class VideoAdsPatch : Patch(
    PatchMetadata(
        "video-ads",
        "YouTube Video Ads Patch",
        "Patch to remove ads in the YouTube video player.",
        compatiblePackages,
        "0.0.1"
    ),
    listOf(
        MethodSignature(
            MethodSignatureMetadata(
                "show-video-ads-constructor",
                MethodMetadata(
                    "zai",
                    "<init>",
                ),
                PatternScanMethod.Fuzzy(2),// FIXME: Test this threshold and find the best value.
                compatiblePackages,
                """Signature for the constructor of some class.
                This signature is being used to find another method in the parent class
                and was discovered in the YouTube version v17.03.38""".trimIndent(),
                "0.0.1"
            ),
            "V",
            AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
            listOf("L", "L", "L"),
            listOf(
                Opcode.INVOKE_DIRECT,
                Opcode.NEW_INSTANCE,
                Opcode.INVOKE_DIRECT,
                Opcode.IPUT_OBJECT,
                Opcode.NEW_INSTANCE,
                null, // either CONST_4 or CONST_16
                Opcode.INVOKE_DIRECT,
                Opcode.IPUT_OBJECT,
                Opcode.NEW_INSTANCE,
                Opcode.INVOKE_DIRECT,
                Opcode.IPUT_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.CONST_4,
                Opcode.IPUT_BOOLEAN,
                Opcode.RETURN_VOID
            )
        )
    )
) {
    override fun execute(patcherData: PatcherData): PatchResult {
        var result = signatures.first().result!!

        val responsibleMethodSignature = MethodSignature(
            MethodSignatureMetadata(
                "show-video-ads-method",
                MethodMetadata(
                    "zai",
                    null // unknown
                ),
                PatternScanMethod.Direct(),
                compatiblePackages,
                "Signature to find the method, which is responsible for showing the video ads",
                "0.0.1"
            ),
            "V",
            AccessFlags.PUBLIC or AccessFlags.FINAL,
            listOf("Z"),
            null
        )

        result = result.findParentMethod(
            responsibleMethodSignature
        ) ?: return PatchResultError(
            "Could not find parent method with signature ${responsibleMethodSignature.metadata.name}"
        )

        // Override the parameter by calling shouldShowAds and setting the parameter to the result
        result.method.implementation!!.addInstructions(
            0,
            """
                invoke-static { }, Lfi/vanced/libraries/youtube/whitelisting/Whitelist;->shouldShowAds()Z
                move-result v1
            """.trimIndent().toInstructions()
        )

        return PatchResultSuccess()
    }
}

