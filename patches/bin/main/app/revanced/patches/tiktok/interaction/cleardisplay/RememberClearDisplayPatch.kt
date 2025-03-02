package app.revanced.patches.tiktok.interaction.cleardisplay

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.tiktok.shared.onRenderFirstFrameFingerprint
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Suppress("unused")
val rememberClearDisplayPatch = bytecodePatch(
    name = "Remember clear display",
    description = "Remembers the clear display configurations in between videos.",
) {
    compatibleWith(
        "com.ss.android.ugc.trill"("36.5.4"),
        "com.zhiliaoapp.musically"("36.5.4"),
    )

    execute {
        onClearDisplayEventFingerprint.method.let {
            // region Hook the "Clear display" configuration save event to remember the state of clear display.

            val isEnabledIndex = it.indexOfFirstInstructionOrThrow(Opcode.IGET_BOOLEAN) + 1
            val isEnabledRegister = it.getInstruction<TwoRegisterInstruction>(isEnabledIndex - 1).registerA

            it.addInstructions(
                isEnabledIndex,
                "invoke-static { v$isEnabledRegister }, " +
                    "Lapp/revanced/extension/tiktok/cleardisplay/RememberClearDisplayPatch;->rememberClearDisplayState(Z)V",
            )

            // endregion

            // region Override the "Clear display" configuration load event to load the state of clear display.

            val clearDisplayEventClass = it.parameters[0].type
            onRenderFirstFrameFingerprint.method.addInstructionsWithLabels(
                0,
                """
                    # Create a new clearDisplayEvent and post it to the EventBus (https://github.com/greenrobot/EventBus)

                    # Clear display type such as 0 = LONG_PRESS, 1 = SCREEN_RECORD etc.
                    const/4 v1, 0x0

                    # Enter method (Such as "pinch", "swipe_exit", or an empty string (unknown, what it means)).
                    const-string v2, ""

                    # Name of the clear display type which is equivalent to the clear display type.
                    const-string v3, "long_press"
                    
                     # The state of clear display.
                    invoke-static { }, Lapp/revanced/extension/tiktok/cleardisplay/RememberClearDisplayPatch;->getClearDisplayState()Z
                    move-result v4
                    if-eqz v4, :clear_display_disabled

                    new-instance v0, $clearDisplayEventClass
                    invoke-direct { v0, v1, v2, v3, v4 }, $clearDisplayEventClass-><init>(ILjava/lang/String;Ljava/lang/String;Z)V
                    invoke-virtual { v0 }, $clearDisplayEventClass->post()Lcom/ss/android/ugc/governance/eventbus/IEvent;
                    """,
                ExternalLabel("clear_display_disabled", onRenderFirstFrameFingerprint.method.getInstruction(0)),
            )

            // endregion
        }
    }
}
