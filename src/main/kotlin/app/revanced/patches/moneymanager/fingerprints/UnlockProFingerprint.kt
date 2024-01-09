package app.revanced.patches.moneymanager.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object UnlockProFingerprint : MethodFingerprint(
    "Z",
    AccessFlags.STATIC or AccessFlags.SYNTHETIC,
    parameters = listOf("L"),
    opcodes = listOf(
        Opcode.IGET_BOOLEAN,
        Opcode.RETURN
    ),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("MainActivity;")
    }
)