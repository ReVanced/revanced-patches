package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

// Resolves to the method CronetDataSource.open
// https://androidx.tech/artifacts/media3/media3-datasource-cronet/1.0.0-alpha03-source/androidx/media3/datasource/cronet/CronetDataSource.java.html
object OpenCronetDataSourceFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.MOVE_RESULT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
    ),
    strings = listOf(
        "err_cleartext_not_permitted",
    ),
)