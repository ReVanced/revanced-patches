package app.revanced.patches.moneymanager.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object UnlockProFingerprint : MethodFingerprint(
    "Z",
    AccessFlags.STATIC or AccessFlags.SYNTHETIC,
    parameters = listOf("L"),
    opcodes = listOf(
        Opcode.IGET_BOOLEAN,
        Opcode.RETURN
    ),
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("MainActivity;")
    }
)