package app.revanced.patches.primevideo.video.speed

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.primevideo.misc.extension.sharedExtensionPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private const val EXTENSION_CLASS_DESCRIPTOR = 
    "Lapp/revanced/extension/primevideo/videoplayer/PlaybackSpeedPatch;"

val playbackSpeedPatch = bytecodePatch(
    name = "Playback speed",
    description = "Adds playback speed controls to the video player.",
) {
    dependsOn(
        sharedExtensionPatch,
    )

    compatibleWith(
        "com.amazon.avod.thirdpartyclient"("3.0.412.2947")
    )

    execute {
        playbackUserControlsInitializeFingerprint.method.apply {
            val getIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.IPUT_OBJECT && 
                getReference<FieldReference>()?.name == "mUserControls"
            }
            
            val getRegister = getInstruction<OneRegisterInstruction>(getIndex).registerA
            
            addInstructions(
                getIndex + 1,
                """
                invoke-static { v$getRegister }, $EXTENSION_CLASS_DESCRIPTOR->initializeSpeedOverlay(Landroid/view/View;)V
                """
            )
        }

        playbackUserControlsPrepareForPlaybackFingerprint.method.apply {
            addInstructions(
                0,
                """
                invoke-virtual { p1 }, Lcom/amazon/avod/playbackclient/PlaybackContext;->getPlayer()Lcom/amazon/video/sdk/player/Player;
                move-result-object v0
                invoke-static { v0 }, $EXTENSION_CLASS_DESCRIPTOR->setPlayer(Lcom/amazon/video/sdk/player/Player;)V
                """
            )
        }
    }
} 