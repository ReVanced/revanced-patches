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
import app.revanced.patches.tiktok.interaction.speed.fingerprints.OnRenderFirstFrameFingerprint
import app.revanced.patches.tiktok.interaction.speed.fingerprints.SpeedControlParentFingerprint
import app.revanced.util.exception
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction11x
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Playback speed",
    description = "Enables the playback speed option for all videos and " +
            "retains the speed configurations in between videos.",
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
        SpeedControlParentFingerprint.result?.mutableMethod?.let { it ->
            val speedControlMethodCallIndex = it.indexOfFirstInstruction {
                val paramsTypes = getReference<MethodReference>()?.parameterTypes
                    ?: return@indexOfFirstInstruction false

                paramsTypes.size == 1 && paramsTypes.first().contains("/Aweme;")
            }

            val enableSpeedControlMethod = context
                .toMethodWalker(it)
                .nextMethod(speedControlMethodCallIndex, true)
                .getMethod() as MutableMethod

            enableSpeedControlMethod.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        } ?: throw SpeedControlParentFingerprint.exception

        GetSpeedFingerprint.result?.mutableMethod?.apply {
            val injectIndex = indexOfFirstInstruction { getReference<MethodReference>()?.returnType == "F" } + 2
            val register = getInstruction<Instruction11x>(injectIndex - 1).registerA

            addInstruction(
                injectIndex,
                "invoke-static { v$register }," +
                        " Lapp/revanced/tiktok/speed/SpeedPatch;->saveDefaultSpeed(F)V"
            )
        } ?: throw GetSpeedFingerprint.exception

        val changeSpeedMethod = ChangeSpeedFingerprint.result?.mutableMethod
            ?: throw ChangeSpeedFingerprint.exception
        OnRenderFirstFrameFingerprint.result?.mutableMethod?.addInstructions(
            0,
        """
                const/4 v0, 0x1
                invoke-virtual {p0, v0}, Lcom/ss/android/ugc/aweme/feed/panel/BaseListFragmentPanel;->getEnterFrom(Z)Ljava/lang/String;
                move-result-object v0
                invoke-virtual {p0}, Lcom/ss/android/ugc/aweme/feed/panel/BaseListFragmentPanel;->getCurrentAweme()Lcom/ss/android/ugc/aweme/feed/model/Aweme;
                move-result-object v1
                invoke-static {}, Lapp/revanced/tiktok/speed/SpeedPatch;->getDefaultSpeed()F
                move-result-object v2
                invoke-static { v0, v1, v2 }, $changeSpeedMethod
            """
        ) ?: throw OnRenderFirstFrameFingerprint.exception
    }
}