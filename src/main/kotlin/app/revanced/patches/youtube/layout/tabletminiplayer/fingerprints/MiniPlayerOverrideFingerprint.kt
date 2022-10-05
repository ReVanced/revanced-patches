package app.revanced.patches.youtube.layout.tabletminiplayer.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.tabletminiplayer.annotations.TabletMiniPlayerCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("mini-player-override-fingerprint")
@TabletMiniPlayerCompatibility
@Version("0.0.1")
object MiniPlayerOverrideFingerprint : MethodFingerprint(
    "Z", AccessFlags.STATIC or AccessFlags.PUBLIC,
    opcodes = listOf(Opcode.RETURN), // anchor to insert the instruction
)