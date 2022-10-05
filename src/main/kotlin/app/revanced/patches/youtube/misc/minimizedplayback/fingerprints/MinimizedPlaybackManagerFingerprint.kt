package app.revanced.patches.youtube.misc.minimizedplayback.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.minimizedplayback.annotations.MinimizedPlaybackCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("minimized-playback-manager-fingerprint")
@FuzzyPatternScanMethod(2) // FIXME: Test this threshold and find the best value.
@MinimizedPlaybackCompatibility
@Version("0.0.1")
object MinimizedPlaybackManagerFingerprint : MethodFingerprint(
    "Z",
    AccessFlags.PUBLIC or AccessFlags.STATIC,
    listOf("L"),
    listOf(
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.IGET,
        Opcode.AND_INT_LIT16,
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT,
        Opcode.IF_NEZ,
        Opcode.SGET_OBJECT,
        Opcode.IGET,
        Opcode.CONST,
        Opcode.IF_NE,
        Opcode.IGET_OBJECT,
        Opcode.IF_NEZ,
        Opcode.SGET_OBJECT,
        Opcode.IGET,
        Opcode.IF_NE,
        Opcode.IGET_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.GOTO,
        Opcode.SGET_OBJECT,
        Opcode.GOTO,
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQZ
    )
)