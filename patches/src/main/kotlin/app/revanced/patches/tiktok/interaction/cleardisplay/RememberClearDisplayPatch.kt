package app.revanced.patches.tiktok.interaction.cleardisplay

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22c

@Suppress("unused")
val rememberClearDisplayPatch = bytecodePatch(
    name = "Remember clear display",
    description = "Remembers the clear display configurations in between videos.",
) {
    compatibleWith(
        "com.ss.android.ugc.trill"("32.5.3"),
        "com.zhiliaoapp.musically"("32.5.3"),
    )

    val onClearDisplayEventMatch by onClearDisplayEventFingerprint()
    val onRenderFirstFrameMatch by onRenderFirstFrameFingerprint()

    execute {
        onClearDisplayEventMatch.mutableMethod.let {
            // region Hook the "Clear display" configuration save event to remember the state of clear display.

            val isEnabledIndex = it.indexOfFirstInstructionOrThrow { opcode == Opcode.IGET_BOOLEAN } + 1
            val isEnabledRegister = it.getInstruction<Instruction22c>(isEnabledIndex - 1).registerA

            it.addInstructions(
                isEnabledIndex,
                "invoke-static { v$isEnabledRegister }, " +
                    "Lapp/revanced/extension/tiktok/cleardisplay/RememberClearDisplayPatch;->rememberClearDisplayState(Z)V",
            )

            // endregion

            // region Override the "Clear display" configuration load event to load the state of clear display.

            val clearDisplayEventClass = it.parameters[0].type
            onRenderFirstFrameMatch.mutableMethod.addInstructionsWithLabels(
                0,
                """
                        # Create a new clearDisplayEvent and post it to the EventBus (https://github.com/greenrobot/EventBus)

                        # The state of clear display.
                        invoke-static { }, Lapp/revanced/extension/tiktok/cleardisplay/RememberClearDisplayPatch;->getClearDisplayState()Z
                        move-result v3
                        if-eqz v3, :clear_display_disabled

                        # Clear display type such as 0 = LONG_PRESS, 1 = SCREEN_RECORD etc.
                        const/4 v1, 0x0

                        # Name of the clear display type which is equivalent to the clear display type.
                        const-string v2, "long_press"

                        new-instance v0, $clearDisplayEventClass
                        invoke-direct { v0, v1, v2, v3 }, $clearDisplayEventClass-><init>(ILjava/lang/String;Z)V
                        invoke-virtual { v0 }, $clearDisplayEventClass->post()Lcom/ss/android/ugc/governance/eventbus/IEvent;
                    """,
                ExternalLabel("clear_display_disabled", onRenderFirstFrameMatch.mutableMethod.getInstruction(0)),
            )
            // endregion
        }
    }
}
