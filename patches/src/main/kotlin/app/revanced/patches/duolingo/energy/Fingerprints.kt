package app.revanced.patches.duolingo.energy

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Matches the class found in [energyConfigToStringFingerprint].
 */
internal val initializeEnergyConfigFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    opcodes(Opcode.RETURN_VOID)
}

// Class name currently is not obfuscated but it may be in the future.
internal val energyConfigToStringFingerprint = fingerprint {
    parameters()
    returns("Ljava/lang/String;")
    strings("EnergyConfig(", "maxEnergy=") // Partial string matches.
    custom { method, _ -> method.name == "toString" }
}
