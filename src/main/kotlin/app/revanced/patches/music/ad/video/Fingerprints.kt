package app.revanced.patches.music.ad.video

import com.android.tools.smali.dexlib2.Opcode
import app.revanced.patcher.fingerprint.methodFingerprint

internal val showMusicVideoAdsParentFingerprint = methodFingerprint {
    opcodes(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
    )
    strings("maybeRegenerateCpnAndStatsClient called unexpectedly, but no error.")
}