package app.revanced.patches.tiktok.interaction.cleardisplay

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tiktok.shared.onRenderFirstFrameFingerprint
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Suppress("unused")
val rememberClearDisplayPatch = bytecodePatch(
    name = "Remember clear display",
    description = "Remembers the clear display configurations in between videos.",
) {
    compatibleWith(
        "com.ss.android.ugc.trill"("43.8.3"),
        "com.zhiliaoapp.musically"("43.8.3"),
    )

    execute {
        // kill loggers to prevent db from being constantly logged to
        // might resolve crashing issue with this patch
        clearModeLogCoreFingerprint.method.returnEarly()
        clearModeLogStateFingerprint.method.returnEarly()
        clearModeLogPlaytimeFingerprint.method.returnEarly()

        onClearDisplayEventFingerprint.method.let {
            // Hook the "Clear display" configuration save event to remember the state of clear display.
            val isEnabledIndex = it.indexOfFirstInstructionOrThrow(Opcode.IGET_BOOLEAN) + 1
            val isEnabledRegister = it.getInstruction<TwoRegisterInstruction>(isEnabledIndex - 1).registerA

            it.addInstructions(
                isEnabledIndex,
                "invoke-static { v$isEnabledRegister }, " +
                    "Lapp/revanced/extension/tiktok/cleardisplay/RememberClearDisplayPatch;->rememberClearDisplayState(Z)V",
            )

            val clearDisplayEventClass = it.parameters[0].type
            onRenderFirstFrameFingerprint.method.addInstructions(
                0,
                """
                    # Get the saved state
                    invoke-static { }, Lapp/revanced/extension/tiktok/cleardisplay/RememberClearDisplayPatch;->getClearDisplayState()Z
                    move-result v1
                    
                    # If false, jump past the event post
                    if-eqz v1, :clear_display_disabled

                    # Set up the other parameters
                    # Clear display type: 0 = LONG_PRESS
                    const/4 v2, 0x0
                    
                    # Enter method
                    const-string v3, ""
                    
                    # Name of the clear display type
                    const-string v4, "long_press"

                    # Create the event
                    new-instance v0, $clearDisplayEventClass
                    
                    # Call the constructor in order
                    invoke-direct { v0, v1, v2, v3, v4 }, $clearDisplayEventClass-><init>(ZILjava/lang/String;Ljava/lang/String;)V
                    
                    # Post it to the EventBus
                    invoke-virtual { v0 }, $clearDisplayEventClass->post()Lcom/ss/android/ugc/governance/eventbus/IEvent;

                    :clear_display_disabled
                    nop
                """
            )
        }
    }
}