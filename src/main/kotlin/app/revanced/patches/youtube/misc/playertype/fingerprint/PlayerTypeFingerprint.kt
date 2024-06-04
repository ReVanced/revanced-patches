package app.revanced.patches.youtube.misc.playertype.fingerprint

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val playerTypeFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    opcodes(
        Opcode.IF_NE,
        Opcode.RETURN_VOID,
    )
    custom { _, classDef -> classDef.endsWith("/YouTubePlayerOverlaysLayout;") }
}
