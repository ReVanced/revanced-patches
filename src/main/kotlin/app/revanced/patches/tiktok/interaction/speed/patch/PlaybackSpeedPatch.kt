package app.revanced.patches.tiktok.interaction.speed.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.tiktok.interaction.speed.annotations.PlaybackSpeedCompatibility
import app.revanced.patches.tiktok.interaction.speed.fingerprints.SpeedControlParentFingerprint
import com.android.tools.smali.dexlib2.Opcode

@Patch
@Name("Playback speed")
@Description("Enables the playback speed option for all videos.")
@PlaybackSpeedCompatibility
class PlaybackSpeedPatch : BytecodePatch(
    listOf(
        SpeedControlParentFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
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
    }
}