package app.revanced.patches.youtube.misc.playertype

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstLiteralInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/PlayerTypeHookPatch;"

internal var reelWatchPlayerId = -1L
    private set

private val playerTypeHookResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    execute {
        reelWatchPlayerId = resourceMappings["id", "reel_watch_player"]
    }
}

val playerTypeHookPatch = bytecodePatch(
    description = "Hook to get the current player type and video playback state.",
) {
    dependsOn(sharedExtensionPatch, playerTypeHookResourcePatch)

    execute {
        playerTypeFingerprint.method.addInstruction(
            0,
            "invoke-static {p1}, $EXTENSION_CLASS_DESCRIPTOR->setPlayerType(Ljava/lang/Enum;)V",
        )

        reelWatchPagerFingerprint.method.apply {
            val literalIndex = indexOfFirstLiteralInstructionOrThrow(reelWatchPlayerId)
            val registerIndex = indexOfFirstInstructionOrThrow(literalIndex, Opcode.MOVE_RESULT_OBJECT)
            val viewRegister = getInstruction<OneRegisterInstruction>(registerIndex).registerA

            addInstruction(
                registerIndex + 1,
                "invoke-static { v$viewRegister }, $EXTENSION_CLASS_DESCRIPTOR->onShortsCreate(Landroid/view/View;)V"
            )
        }

        videoStateFingerprint.method.apply {
            val endIndex = videoStateFingerprint.patternMatch!!.endIndex
            val videoStateFieldName = getInstruction<ReferenceInstruction>(endIndex).reference

            addInstructions(
                0,
                """
                    iget-object v0, p1, $videoStateFieldName  # copy VideoState parameter field
                    invoke-static {v0}, $EXTENSION_CLASS_DESCRIPTOR->setVideoState(Ljava/lang/Enum;)V
                """
            )
        }
    }
}
