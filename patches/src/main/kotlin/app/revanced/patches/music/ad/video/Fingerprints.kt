package app.revanced.patches.music.ad.video

import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.opcodes
import com.android.tools.smali.dexlib2.Opcode

internal val showVideoAdsParentMethodMatch = firstMethodComposite(
    "maybeRegenerateCpnAndStatsClient called unexpectedly, but no error.",
) {
    opcodes(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
    )
}
