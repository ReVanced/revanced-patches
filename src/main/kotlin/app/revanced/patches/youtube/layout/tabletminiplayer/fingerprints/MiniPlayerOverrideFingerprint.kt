package app.revanced.patches.youtube.layout.tabletminiplayer.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object MiniPlayerOverrideFingerprint : MethodFingerprint(
    "Z", AccessFlags.STATIC or AccessFlags.PUBLIC,
    opcodes = listOf(Opcode.RETURN), // anchor to insert the instruction
)