package app.revanced.patches.tiktok.interaction.speed

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.tiktok.interaction.speed.fingerprints.SpeedControlParentFingerprint
import app.revanced.util.exception
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Playback speed",
    description = "Enables the playback speed option for all videos.",
    compatiblePackages = [
        CompatiblePackage("com.ss.android.ugc.trill", ["32.5.3"]),
        CompatiblePackage("com.zhiliaoapp.musically", ["32.5.3"])
    ]
)
@Suppress("unused")
object PlaybackSpeedPatch : BytecodePatch(setOf(SpeedControlParentFingerprint)) {
    override fun execute(context: BytecodeContext) {
        SpeedControlParentFingerprint.result?.mutableMethod?.apply {
            val targetMethodCallIndex = indexOfFirstInstruction {
                if (opcode == Opcode.INVOKE_STATIC) {
                    val paramsTypes = ((this as Instruction35c).reference as MethodReference).parameterTypes
                    paramsTypes.size == 1 && paramsTypes[0].contains("/Aweme;")
                } else false
            }

            val isSpeedEnableMethod = context
                .toMethodWalker(this)
                .nextMethod(targetMethodCallIndex, true)
                .getMethod() as MutableMethod

            isSpeedEnableMethod.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        } ?: throw SpeedControlParentFingerprint.exception
    }
}