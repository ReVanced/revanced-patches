package app.revanced.patches.duolingo.ad

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val initializeMonetizationDebugSettingsFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    // Parameters have not been reliable for fingerprinting between versions.
    opcodes(Opcode.IPUT_BOOLEAN)
}

internal val monetizationDebugSettingsToStringFingerprint = fingerprint {
    strings("MonetizationDebugSettings(") // Partial string match.
    custom { method, _ -> method.name == "toString" }
}