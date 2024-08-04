package app.revanced.patches.music.ad.video.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object ShowVideoAdsParentFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
    ),
    strings = listOf("maybeRegenerateCpnAndStatsClient called unexpectedly, but no error."),
)
