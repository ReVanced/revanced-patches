package app.revanced.patches.music.ad.video

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.opcodes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.showVideoAdsParentMethodMatch by composingFirstMethod(
    "maybeRegenerateCpnAndStatsClient called unexpectedly, but no error.",
) {
    opcodes(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
    )
}
