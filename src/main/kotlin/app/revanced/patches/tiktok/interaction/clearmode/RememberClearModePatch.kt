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
    name = "Clear display",
    description = "Remembers the clear display configurations in between videos.",
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

            // Catches the clear mode configuration changed event and saved that configuration
            // to apply to other videos.
            it.apply {
                val injectIndex = indexOfFirstInstruction { opcode == Opcode.IGET_BOOLEAN } + 1
                val register = getInstruction<Instruction22c>(injectIndex - 1).registerA

                addInstructions(
                    injectIndex,
                    "invoke-static { v$register }, " +
                            "Lapp/revanced/tiktok/clearmode/RememberClearModePatch;->rememberClearModeState(Z)V"
                )
            }

            // Changes the clear mode configuration on the first frame of video.
            // Because the default behavior of TikTok is turn off clear mode when swiping to next video.
            val clearModeEventClass = it.parameters[0].type
            OnRenderFirstFrameFingerprint.result?.mutableMethod?.addInstructions(
                0,
                """
                    # These instructions will create a clearModeEvent which will be posted to notify other 
                    # app components. TikTok use https://github.com/greenrobot/EventBus 
                    # To create a new clearModeEvent we need 3 arguments
                    # First is a Integer which present the type of clear mode such as 0 = LONG_PRESS, 1 = SCREEN_RECORD,...
                    new-instance v0, $clearModeEventClass
                    const/4 v1, 0x0
                    # Second is a String which is similar to the first but as String.
                    const-string v2, "long_press"
                    # Third is a Boolean which is the state of clear mode.
                    invoke-static {}, Lapp/revanced/tiktok/clearmode/RememberClearModePatch;->getClearModeState()Z
                    move-result v3
                    invoke-direct {v0, v1, v2, v3}, $clearModeEventClass-><init>(ILjava/lang/String;Z)V
                    invoke-virtual {v0}, $clearModeEventClass->post()Lcom/ss/android/ugc/governance/eventbus/IEvent;
                """
            ) ?: throw OnRenderFirstFrameFingerprint.exception
        } ?: throw OnClearModeEventFingerprint.exception
    }
}
