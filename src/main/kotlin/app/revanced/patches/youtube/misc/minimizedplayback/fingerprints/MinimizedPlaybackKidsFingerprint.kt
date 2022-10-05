package app.revanced.patches.youtube.misc.minimizedplayback.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.minimizedplayback.annotations.MinimizedPlaybackCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("minimized-playback-kids-fingerprint")
@MinimizedPlaybackCompatibility
@Version("0.0.1")
object MinimizedPlaybackKidsFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    listOf("I", "L", "L"),
    listOf(
        Opcode.IF_EQZ,
        Opcode.SGET_OBJECT,
        Opcode.IF_NE,
        Opcode.CONST_4,
        Opcode.IPUT_BOOLEAN,
        Opcode.IF_EQZ,
        Opcode.IGET,
        Opcode.INVOKE_STATIC
    ),
    customFingerprint = { it.definingClass.endsWith("MinimizedPlaybackPolicyController;") }
)
