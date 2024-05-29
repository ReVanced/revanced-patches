package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val createPlayerRequestBodyFingerprint = methodFingerprint {
    returns("V")
    parameters("L")
    opcodes(
        Opcode.CHECK_CAST,
        Opcode.IGET,
        Opcode.AND_INT_LIT16,
    )
    strings("ms")
}
