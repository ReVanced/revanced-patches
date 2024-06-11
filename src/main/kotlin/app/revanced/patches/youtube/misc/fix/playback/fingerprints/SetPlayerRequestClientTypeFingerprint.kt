package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.Opcode

internal val setPlayerRequestClientTypeFingerprint = methodFingerprint {
    opcodes(
        Opcode.IGET,
        Opcode.IPUT, // Sets ClientInfo.clientId.
    )
    strings("10.29")
    literal { 134217728 }
}
