package app.revanced.patches.tiktok.interaction.clearmode

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.tiktok.interaction.clearmode.fingerprints.OnClearModeEventFingerprint
import app.revanced.patches.tiktok.interaction.clearmode.fingerprints.OnRenderFirstFrameFingerprint
import app.revanced.util.exception
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22c

@Patch(
    name = "Remember clear mode",
    description = "Remembers the clear mode configurations in between videos.",
    compatiblePackages = [
        CompatiblePackage("com.ss.android.ugc.trill", ["32.5.3"]),
        CompatiblePackage("com.zhiliaoapp.musically", ["32.5.3"])
    ]
)
@Suppress("unused")
object RememberClearModePatch : BytecodePatch(
    setOf(
        OnClearModeEventFingerprint,
        OnRenderFirstFrameFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        OnClearModeEventFingerprint.result?.mutableMethod?.let {
            // region Hook the "Clear mode" configuration save event to remember the state of clear mode.

            val isEnabledIndex = it.indexOfFirstInstruction { opcode == Opcode.IGET_BOOLEAN } + 1
            val isEnabledRegister = it.getInstruction<Instruction22c>(isEnabledIndex - 1).registerA

            it.addInstructions(
                isEnabledIndex,
                "invoke-static { v$isEnabledRegister }, " +
                        "Lapp/revanced/tiktok/clearmode/RememberClearModePatch;->rememberClearModeState(Z)V"
            )

            // endregion

            // region Override the "Clear mode" configuration load event to load the state of clear mode.

            val clearModeEventClass = it.parameters[0].type
            OnRenderFirstFrameFingerprint.result?.mutableMethod?.apply {
                addInstructionsWithLabels(
                    0,
                    """
                        # Create a new clearModeEvent and post it to the EventBus (https://github.com/greenrobot/EventBus)

                        # The state of clear mode.
                        invoke-static { }, Lapp/revanced/tiktok/clearmode/RememberClearModePatch;->getClearModeState()Z
                        move-result v3
                        if-eqz v3, :clear_mode_disabled

                        # Clear mode type such as 0 = LONG_PRESS, 1 = SCREEN_RECORD etc.
                        const/4 v1, 0x0

                        # Name of the clear mode type which is equivalent to the clear mode type.
                        const-string v2, "long_press"

                        new-instance v0, $clearModeEventClass
                        invoke-direct { v0, v1, v2, v3 }, $clearModeEventClass-><init>(ILjava/lang/String;Z)V
                        invoke-virtual { v0 }, $clearModeEventClass->post()Lcom/ss/android/ugc/governance/eventbus/IEvent;
                    """,
                    ExternalLabel("clear_mode_disabled", getInstruction(0))
                )
            } ?: throw OnRenderFirstFrameFingerprint.exception

            // endregion
        } ?: throw OnClearModeEventFingerprint.exception
    }
}
