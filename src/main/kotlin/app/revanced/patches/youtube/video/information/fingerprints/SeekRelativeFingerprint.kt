package app.revanced.patches.youtube.video.information.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Resolves using class found in [PlayerInitFingerprint].
 */
internal object SeekRelativeFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "Z",
    parameters = listOf("J", "L"),
    opcodes = listOf(
        Opcode.ADD_LONG_2ADDR,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.RETURN
    )
)