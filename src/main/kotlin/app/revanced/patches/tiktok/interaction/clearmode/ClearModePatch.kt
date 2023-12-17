package app.revanced.patches.tiktok.interaction.clearmode

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.tiktok.interaction.clearmode.fingerprints.OnClearModeEventFingerprint
import app.revanced.patches.tiktok.interaction.clearmode.fingerprints.OnRenderFirstFrameFingerprint
import app.revanced.util.exception
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22c

@Patch(
    name = "Clear mode",
    description = "Retains the clear mode configurations in between videos.",
    compatiblePackages = [
        CompatiblePackage("com.ss.android.ugc.trill", ["32.5.3"]),
        CompatiblePackage("com.zhiliaoapp.musically", ["32.5.3"])
    ]
)
@Suppress("unused")
object ClearModePatch : BytecodePatch(
    setOf(
        OnClearModeEventFingerprint,
        OnRenderFirstFrameFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        OnClearModeEventFingerprint.result?.mutableMethod?.let {

            it.apply {
                val injectIndex = indexOfFirstInstruction { opcode == Opcode.IGET_BOOLEAN } + 1
                val register = getInstruction<Instruction22c>(injectIndex - 1).registerA

                addInstructions(
                    injectIndex,
                    "invoke-static { v$register }, " +
                            "Lapp/revanced/tiktok/clearmode/ClearModePatch;->saveClearModeState(Z)V"
                )
            }

            val clearModeEventClass = it.parameters[0].type
            OnRenderFirstFrameFingerprint.result?.mutableMethod?.addInstructions(
                0,
                """
                    new-instance v0, $clearModeEventClass
                    const/4 v1, 0x0
                    const-string v2, "long_press"
                    invoke-static {}, Lapp/revanced/tiktok/clearmode/ClearModePatch;->getClearModeState()Z
                    move-result v3
                    invoke-direct {v0, v1, v2, v3}, $clearModeEventClass-><init>(ILjava/lang/String;Z)V
                    invoke-virtual {v0}, $clearModeEventClass->post()Lcom/ss/android/ugc/governance/eventbus/IEvent;
                """
            ) ?: throw OnRenderFirstFrameFingerprint.exception
        } ?: throw OnClearModeEventFingerprint.exception
    }
}
