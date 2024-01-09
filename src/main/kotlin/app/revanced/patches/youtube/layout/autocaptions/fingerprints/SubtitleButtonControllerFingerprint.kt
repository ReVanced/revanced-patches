package app.revanced.patches.youtube.layout.autocaptions.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object SubtitleButtonControllerFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("Lcom/google/android/libraries/youtube/player/subtitles/model/SubtitleTrack;"),
    opcodes = listOf(
        Opcode.IGET_OBJECT,
        Opcode.IF_NEZ,
        Opcode.RETURN_VOID,
        Opcode.IGET_BOOLEAN,
        Opcode.CONST_4,
        Opcode.IF_NEZ,
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
    )
)