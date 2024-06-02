package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Resolves using the class found in [StoryboardThumbnailParentFingerprint].
 */
@Deprecated("Fingerprint is obsolete and will be deleted soon")
internal object StoryboardThumbnailFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "Z",
    parameters = listOf(),
    opcodes = listOf(
        Opcode.MOVE_RESULT,
        Opcode.IF_GTZ,
        Opcode.GOTO,
        Opcode.CONST_4,
        Opcode.RETURN,
        Opcode.RETURN, // Last instruction of method.
    ),
)