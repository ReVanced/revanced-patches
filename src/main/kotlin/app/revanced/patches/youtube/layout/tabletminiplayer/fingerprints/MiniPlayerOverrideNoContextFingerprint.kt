package app.revanced.patches.youtube.layout.tabletminiplayer.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.tabletminiplayer.annotations.TabletMiniPlayerCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("mini-player-override-no-context-fingerprint")
@TabletMiniPlayerCompatibility
@Version("0.0.1")
object MiniPlayerOverrideNoContextFingerprint : MethodFingerprint(
    "Z", AccessFlags.FINAL or AccessFlags.PRIVATE,
    opcodes = listOf(Opcode.RETURN), // anchor to insert the instruction
)