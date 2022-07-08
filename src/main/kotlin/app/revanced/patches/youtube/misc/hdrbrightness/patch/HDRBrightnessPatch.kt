package app.revanced.patches.youtube.misc.hdrbrightness.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.youtube.misc.hdrbrightness.annotations.HDRBrightnessCompatibility
import app.revanced.patches.youtube.misc.hdrbrightness.fingerprints.HDRBrightnessFingerprint
import org.jf.dexlib2.iface.instruction.NarrowLiteralInstruction
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@Name("hdr-max-brightness")
@Description("Sets brightness to max for HDR videos in fullscreen mode.")
@HDRBrightnessCompatibility
@Version("0.0.1")
class HDRBrightnessPatch : BytecodePatch(
    listOf(
        HDRBrightnessFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val result = HDRBrightnessFingerprint.result
            ?: return PatchResultError("HDRBrightnessFingerprint could not resolve the method!")


        val method = result.mutableMethod

        //Get the index here, so we know where to inject our code to override -1.0f
        val index = method.implementation!!.instructions.indexOfFirst { ((it as? NarrowLiteralInstruction)?.narrowLiteral == (-1.0f).toRawBits()) }
        val register = (method.implementation!!.instructions.get(index) as OneRegisterInstruction).registerA

        method.addInstructions(
            index + 1, """
           invoke-static {v$register}, Lapp/revanced/integrations/patches/HDRMaxBrightnessPatch;->getHDRBrightness(F)F
           move-result v$register
        """
        )

        return PatchResultSuccess()
    }
}
