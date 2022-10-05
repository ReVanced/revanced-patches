package app.revanced.patches.music.audio.exclusiveaudio.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.music.audio.exclusiveaudio.annotations.ExclusiveAudioCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("audio-only-enabler-fingerprint")

@ExclusiveAudioCompatibility
@Version(
    "0.0.1"
)
object  AudioOnlyEnablerFingerprint: MethodFingerprint(
    "Z", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf(), listOf(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.IF_NEZ,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.GOTO,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.RETURN
    )
)