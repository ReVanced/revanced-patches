package app.revanced.patches.youtube.layout.hide.floatingmicrophone.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.hide.floatingmicrophone.patch.HideFloatingMicrophoneButtonResourcePatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction

object ShowFloatingMicrophoneButtonFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQZ,
        Opcode.RETURN_VOID
    ),
    customFingerprint = { methodDef ->
        methodDef.implementation?.instructions?.any {
            (it as? WideLiteralInstruction)?.wideLiteral == HideFloatingMicrophoneButtonResourcePatch.fabButtonId
        } == true
    }
)