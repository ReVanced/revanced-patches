package app.revanced.patches.shared.opus

import app.revanced.util.fingerprint.legacyFingerprint
import app.revanced.util.or
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val codecReferenceFingerprint = legacyFingerprint(
    name = "codecReferenceFingerprint",
    returnType = "J",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L"),
    opcodes = listOf(Opcode.INVOKE_SUPER),
    strings = listOf("itag")
)

internal val codecSelectorFingerprint = legacyFingerprint(
    name = "codecSelectorFingerprint",
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    opcodes = listOf(
        Opcode.NEW_INSTANCE,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT
    ),
    strings = listOf("Audio track id %s not in audio streams")
)

