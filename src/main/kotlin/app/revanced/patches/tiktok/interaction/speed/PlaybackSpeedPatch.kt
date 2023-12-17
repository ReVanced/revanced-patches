package app.revanced.patches.tiktok.interaction.speed

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.tiktok.interaction.speed.fingerprints.ChangeSpeedFingerprint
import app.revanced.patches.tiktok.interaction.speed.fingerprints.GetSpeedFingerprint
import app.revanced.patches.tiktok.share.fingerprints.OnRenderFirstFrameFingerprint
import app.revanced.patches.tiktok.interaction.speed.fingerprints.SpeedControlParentFingerprint
import app.revanced.util.exception
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction11x
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Playback speed",
    description = "Enables the playback speed option for all videos and retains the speed configurations in between videos.",
    compatiblePackages = [
        CompatiblePackage("com.ss.android.ugc.trill", ["32.5.3"]),
        CompatiblePackage("com.zhiliaoapp.musically", ["32.5.3"])
    ]
)
@Suppress("unused")
object PlaybackSpeedPatch : BytecodePatch(
    setOf(
        SpeedControlParentFingerprint,
        GetSpeedFingerprint,
        OnRenderFirstFrameFingerprint,
        ChangeSpeedFingerprint
    )
) {
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

        GetSpeedFingerprint.result?.mutableMethod?.apply {
            val injectIndex = indexOfFirstInstruction {
                opcode == Opcode.INVOKE_STATIC && ((this as Instruction35c).reference as MethodReference).returnType == "F"
            } + 2
            val reg = (getInstruction(injectIndex - 1) as Instruction11x).registerA
            addInstruction(
                injectIndex,
                "invoke-static { v$reg }, Lapp/revanced/tiktok/speed/SpeedPatch;->saveDefaultSpeed(F)V"
            )
        } ?: throw GetSpeedFingerprint.exception

        OnRenderFirstFrameFingerprint.result?.mutableMethod?.apply {
            ChangeSpeedFingerprint.result?.mutableMethod?.let { changeSpeedMethod ->
                addInstructions(
                    0,
                    """
                        const/4 v0, 0x1
                        invoke-virtual {p0, v0}, Lcom/ss/android/ugc/aweme/feed/panel/BaseListFragmentPanel;->getEnterFrom(Z)Ljava/lang/String;
                        move-result-object v0
                        invoke-virtual {p0}, Lcom/ss/android/ugc/aweme/feed/panel/BaseListFragmentPanel;->getCurrentAweme()Lcom/ss/android/ugc/aweme/feed/model/Aweme;
                        move-result-object v1
                        invoke-static {}, Lapp/revanced/tiktok/speed/SpeedPatch;->getDefaultSpeed()F
                        move-result-object v2
                        invoke-static { v0, v1, v2 }, ${changeSpeedMethod.definingClass}->${changeSpeedMethod.name}(${changeSpeedMethod.parameterTypes[0]}${changeSpeedMethod.parameterTypes[1]}${changeSpeedMethod.parameterTypes[2]})V
                    """
                )
            } ?: throw ChangeSpeedFingerprint.exception
        } ?: throw OnRenderFirstFrameFingerprint.exception
    }
}