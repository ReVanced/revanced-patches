package app.revanced.patches.duolingo.ad

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val initializeMonetizationDebugSettingsMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returnType("V")
    // Parameters have not been reliable for fingerprinting between versions.
    opcodes(Opcode.IPUT_BOOLEAN)
}

internal val BytecodePatchContext.monetizationDebugSettingsToStringMethod by gettingFirstMutableMethodDeclaratively {
    name("toString")
    instructions(string("MonetizationDebugSettings(", String::contains))
}
