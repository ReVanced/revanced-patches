package app.revanced.patches.duolingo.energy

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val ClassDef.initializeEnergyConfigMethodMatch by ClassDefComposing.composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    opcodes(Opcode.RETURN_VOID)
}

// Class name currently is not obfuscated, but it may be in the future.
internal val BytecodePatchContext.energyConfigToStringMethod by gettingFirstMethodDeclaratively {
    name("toString")
    parameterTypes()
    returnType("Ljava/lang/String;")
    instructions(
        string("EnergyConfig(", String::contains),
        string("maxEnergy=", String::contains),
    )
}
