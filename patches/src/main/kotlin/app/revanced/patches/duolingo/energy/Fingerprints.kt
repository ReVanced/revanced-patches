package app.revanced.patches.duolingo.energy

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Matches the class found in [initializeEnergyConfigMethod].
 */
internal val BytecodePatchContext.initializeEnergyConfigMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    opcodes(Opcode.RETURN_VOID)
}

// Class name currently is not obfuscated, but it may be in the future.
internal val BytecodePatchContext.energyConfigToStringMethod by gettingFirstMutableMethodDeclaratively(
    "EnergyConfig(", "maxEnergy=" // Partial string matches.
) {
    name("toString")
    parameterTypes()
    returnType("Ljava/lang/String;")
}
