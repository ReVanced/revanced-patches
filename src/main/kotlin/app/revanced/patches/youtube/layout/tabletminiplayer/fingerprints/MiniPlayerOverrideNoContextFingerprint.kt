package app.revanced.patches.youtube.layout.tabletminiplayer.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object MiniPlayerOverrideNoContextFingerprint : MethodFingerprint(
    "Z", AccessFlags.FINAL or AccessFlags.PRIVATE,
    opcodes = listOf(Opcode.RETURN), // anchor to insert the instruction
)