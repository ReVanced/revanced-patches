package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val setPlayerRequestClientTypeFingerprint = methodFingerprint(
    literal { 134217728 },
) {
    opcodes(
        Opcode.IGET,
        Opcode.IPUT, // Sets ClientInfo.clientId.
    )
    strings("10.29")
}
