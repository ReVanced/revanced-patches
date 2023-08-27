package app.revanced.patches.youtube.layout.hide.floatingmicrophone.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.util.patch.LiteralValueFingerprint
import app.revanced.patches.youtube.layout.hide.floatingmicrophone.patch.HideFloatingMicrophoneButtonResourcePatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object ShowFloatingMicrophoneButtonFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "V",
    parameters = listOf(),
    opcodes = listOf(
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQZ,
        Opcode.RETURN_VOID
    ),
    literal = HideFloatingMicrophoneButtonResourcePatch.fabButtonId
)