package app.revanced.patches.yuka.misc.unlockpremium.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val isPremiumFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    opcodes(
        Opcode.IGET_BOOLEAN,
        Opcode.RETURN,
    )
}
