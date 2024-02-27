package app.revanced.patches.youtube.video.speed.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object PlaybackSpeedInitializeFingerprint : MethodFingerprint(
    returnType = "F",
    accessFlags = AccessFlags.PRIVATE or AccessFlags.STATIC,
    parameters = listOf("L"),
    opcodes = listOf(
        Opcode.IGET,
        Opcode.RETURN
    )
)
