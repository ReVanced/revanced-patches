package app.revanced.patches.youtube.layout.hide.player.overlay.bytecode.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.hide.player.overlay.resource.patch.HidePlayerOverlayResourcePatch
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction

object CreatePlayerOverviewFingerprint : MethodFingerprint(
    returnType = "V",
    access = AccessFlags.PRIVATE or AccessFlags.FINAL,
    opcodes = listOf(
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST
    ),
    customFingerprint = { methodDef ->
        methodDef.implementation?.instructions?.any {
            if (it.opcode != Opcode.CONST) return@any false

            val literal = (it as WideLiteralInstruction).wideLiteral

            literal == HidePlayerOverlayResourcePatch.scrimOverlayId
        } ?: false
    }
)