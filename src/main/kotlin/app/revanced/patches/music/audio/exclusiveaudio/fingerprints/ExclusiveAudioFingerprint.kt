package app.revanced.patches.music.audio.exclusiveaudio.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.music.audio.exclusiveaudio.annotations.ExclusiveAudioCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("exclusive-audio-fingerprints")
@FuzzyPatternScanMethod(2) // FIXME: Test this threshold and find the best value.
@ExclusiveAudioCompatibility
@Version("0.0.1")
object ExclusiveAudioFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    listOf("L", "Z"),
    listOf(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQ,
        Opcode.CONST_4,
        Opcode.GOTO,
        Opcode.NOP,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.IF_EQZ,
        Opcode.IF_EQZ,
        Opcode.INVOKE_INTERFACE,
        Opcode.INVOKE_INTERFACE,
        Opcode.GOTO,
        Opcode.RETURN_VOID
    )
)