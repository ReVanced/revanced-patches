package app.revanced.patches.music.layout.premium.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val hideGetPremiumFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    opcodes(
        Opcode.IF_NEZ,
        Opcode.CONST_16,
        Opcode.INVOKE_VIRTUAL,
    )
    strings("FEmusic_history", "FEmusic_offline")
}