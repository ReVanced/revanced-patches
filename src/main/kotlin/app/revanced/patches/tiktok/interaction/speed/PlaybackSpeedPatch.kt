package app.revanced.patches.tiktok.interaction.speed

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.tiktok.interaction.speed.fingerprints.SpeedControlParentFingerprint
import com.android.tools.smali.dexlib2.Opcode

@Patch(
    name = "Playback speed",
    description = "Enables the playback speed option for all videos.",
    compatiblePackages = [
        CompatiblePackage("com.ss.android.ugc.trill"),
        CompatiblePackage("com.zhiliaoapp.musically")
    ]
)
@Suppress("unused")
object PlaybackSpeedPatch : BytecodePatch(setOf(SpeedControlParentFingerprint)) {
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