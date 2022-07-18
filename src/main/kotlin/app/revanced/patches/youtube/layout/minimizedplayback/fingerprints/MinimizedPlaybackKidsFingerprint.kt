package app.revanced.patches.youtube.layout.minimizedplayback.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.minimizedplayback.annotations.MinimizedPlaybackCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("minimized-playback-kids-fingerprint")
@MatchingMethod(
    "LMinimizedPlaybackPolicyController;", "k"
)
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
    null,
    { it.definingClass.endsWith("MinimizedPlaybackPolicyController;") }
)
