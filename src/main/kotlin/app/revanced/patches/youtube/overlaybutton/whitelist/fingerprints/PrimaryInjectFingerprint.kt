package app.revanced.patches.youtube.overlaybutton.whitelist.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object PrimaryInjectFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = emptyList(),
    opcodes = listOf(
        Opcode.IF_NEZ,
        Opcode.CONST_STRING,
        Opcode.INVOKE_STATIC,
        Opcode.RETURN_VOID,
        Opcode.IGET_OBJECT
    ),
    strings = listOf("play() called when the player wasn\'t loaded.")
)