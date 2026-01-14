package app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy

import app.revanced.patcher.*
import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.piracyDetectionMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("V")
    instructions(
        Opcode.NEW_INSTANCE(),
        Opcode.CONST_16(),
        Opcode.CONST_WIDE_16(),
        Opcode.INVOKE_DIRECT(),
        Opcode.INVOKE_VIRTUAL(),
        Opcode.RETURN_VOID()
    )
    definingClass("ProcessLifeCyleListener;"::endsWith)
}
