package app.revanced.patches.music.ad.video

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.opcodes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.strings
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.showVideoAdsParentMethod by gettingFirstMethodDeclaratively {
    opcodes(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
    )
    strings("maybeRegenerateCpnAndStatsClient called unexpectedly, but no error.")
}
