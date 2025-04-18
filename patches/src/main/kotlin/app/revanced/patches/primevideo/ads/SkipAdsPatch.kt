package app.revanced.patches.primevideo.ads

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val skipAdsPatch = bytecodePatch(
    name = "Skip ads",
    description = "Automatically skip video stream ads.",
) {
    compatibleWith("com.amazon.avod.thirdpartyclient"("3.0.403.257"))

    dependsOn(sharedExtensionPatch("primevideo"))

    // We're going to skip all the logic in ServerInsertedAdBreakState.enter(), which plays all the ad clips in this
    // ad break. Instead, we'll force the video player to seek over the entire break and reset the state machine.
    execute {
        // Force doTrigger() access to public so we can call it from our extension.
        doTriggerFingerprint.method.accessFlags = AccessFlags.PUBLIC.value;

        val getPlayerIndex = enterServerInsertedAdBreakStateFingerprint.patternMatch!!.startIndex
        enterServerInsertedAdBreakStateFingerprint.method.apply {
            // Get register that stores VideoPlayer:
            //  invoke-virtual ->getPrimaryPlayer()
            //  move-result-object {vx}
            val playerRegister = getInstruction<OneRegisterInstruction>(getPlayerIndex + 1).registerA

            // Reuse the params from the original method:
            //  p0 = ServerInsertedAdBreakState
            //  p1 = AdBreakTrigger
            addInstructions(
                getPlayerIndex + 2,
                """
                    invoke-static {p0, p1, v${playerRegister}}, Lapp/revanced/extension/primevideo/ads/SkipAdsPatch;->ServerInsertedAdBreakState_enter(Lcom/amazon/avod/media/ads/internal/state/ServerInsertedAdBreakState;Lcom/amazon/avod/media/ads/internal/state/AdBreakTrigger;Lcom/amazon/avod/media/playback/VideoPlayer;)V
                    return-void
                """
            )
        }
    }
}

