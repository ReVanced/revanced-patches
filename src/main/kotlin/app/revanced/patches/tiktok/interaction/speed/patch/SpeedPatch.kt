package app.revanced.patches.tiktok.interaction.speed.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.data.toMethodWalker
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.tiktok.interaction.speed.annotations.SpeedCompatibility
import app.revanced.patches.tiktok.interaction.speed.fingerprints.SpeedControlParentFingerprint
import org.jf.dexlib2.Opcode

@Patch
@Name("tiktok-speed")
@Description("Enables the playback speed option for all videos.")
@SpeedCompatibility
@Version("0.0.1")
class SpeedPatch : BytecodePatch(
    listOf(
        SpeedControlParentFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val parentMethod = SpeedControlParentFingerprint.result!!.mutableMethod
        val parentMethodInstructions = parentMethod.implementation!!.instructions
        for ((index, instruction) in parentMethodInstructions.withIndex()) {
            if (instruction.opcode != Opcode.INVOKE_VIRTUAL) continue
            val isSpeedEnableMethod = context
                .toMethodWalker(parentMethod)
                .nextMethod(index, true)
                .getMethod() as MutableMethod
            isSpeedEnableMethod.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
            break
        }
        return PatchResultSuccess()
    }
}