package app.revanced.patches.music.layout.minimizedplayback.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.music.layout.minimizedplayback.annotations.MinimizedPlaybackCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("minimized-playback-manager-fingerprint")
@MinimizedPlaybackCompatibility
@Version("0.0.1")
object MinimizedPlaybackManagerFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    listOf("I", "L", "Z"),
    listOf(
        Opcode.IGET,
        Opcode.IF_NE,
        Opcode.IGET_OBJECT,
        Opcode.IF_NE,
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQ,
        Opcode.GOTO,
        Opcode.RETURN_VOID,
        Opcode.SGET_OBJECT,
        Opcode.CONST_4,
        Opcode.IF_NE,
        Opcode.IPUT_BOOLEAN,
    )
)
