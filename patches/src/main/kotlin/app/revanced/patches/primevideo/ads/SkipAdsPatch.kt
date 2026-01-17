package app.revanced.patches.primevideo.ads

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.primevideo.misc.extension.sharedExtensionPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Suppress("unused", "ObjectPropertyName")
val `Skip ads` by creatingBytecodePatch(
    description = "Automatically skips video stream ads.",
) {
    compatibleWith("com.amazon.avod.thirdpartyclient"("3.0.412.2947"))

    dependsOn(sharedExtensionPatch)

    // Skip all the logic in ServerInsertedAdBreakState.enter(), which plays all the ad clips in this
    // ad break. Instead, force the video player to seek over the entire break and reset the state machine.
    apply {
        // Force doTrigger() access to public so we can call it from our extension.
        doTriggerMethod.accessFlags = AccessFlags.PUBLIC.value

        enterServerInsertedAdBreakStateMethod.apply {
            // Get register that stores VideoPlayer:
            //  invoke-virtual ->getPrimaryPlayer()
            //  move-result-object { playerRegister }
            val getPlayerIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_VIRTUAL &&
                        getReference<MethodReference>()?.name == "getPrimaryPlayer"
            }

            val playerRegister = getInstruction<OneRegisterInstruction>(getPlayerIndex + 1).registerA

            // Reuse the params from the original method:
            //  p0 = ServerInsertedAdBreakState
            //  p1 = AdBreakTrigger
            addInstructions(
                getPlayerIndex + 2,
                """
                    invoke-static { p0, p1, v$playerRegister }, Lapp/revanced/extension/primevideo/ads/SkipAdsPatch;->enterServerInsertedAdBreakState(Lcom/amazon/avod/media/ads/internal/state/ServerInsertedAdBreakState;Lcom/amazon/avod/media/ads/internal/state/AdBreakTrigger;Lcom/amazon/avod/media/playback/VideoPlayer;)V
                    return-void
                """
            )
        }
    }
}
