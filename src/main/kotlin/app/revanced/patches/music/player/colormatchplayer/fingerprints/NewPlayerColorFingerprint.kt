package app.revanced.patches.music.player.colormatchplayer.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object NewPlayerColorFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L", "J"),
    opcodes = listOf(
        Opcode.INVOKE_DIRECT,
        Opcode.RETURN_VOID,
        Opcode.INVOKE_DIRECT,
        Opcode.RETURN_VOID
    )
)