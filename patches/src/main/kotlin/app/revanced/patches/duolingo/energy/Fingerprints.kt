package app.revanced.patches.duolingo.energy

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val initializeEnergyConfigFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    opcodes(Opcode.IPUT)
}

internal val energyConfigToStringFingerprint = fingerprint {
    strings("EnergyConfig(") // Partial string match.
    custom { method, _ -> method.name == "toString" }
}
