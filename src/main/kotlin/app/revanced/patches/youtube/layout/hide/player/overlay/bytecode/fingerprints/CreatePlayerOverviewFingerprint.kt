package app.revanced.patches.youtube.layout.hide.player.overlay.bytecode.fingerprints

import app.revanced.extensions.containsConstantInstructionValue
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.hide.player.overlay.resource.patch.HidePlayerOverlayResourcePatch
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object CreatePlayerOverviewFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PRIVATE or AccessFlags.FINAL,
    opcodes = listOf(
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST
    ),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("YouTubeControlsOverlay;")
                &&  methodDef.containsConstantInstructionValue(HidePlayerOverlayResourcePatch.scrimOverlayId)
    }
)