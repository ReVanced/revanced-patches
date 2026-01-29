package app.revanced.patches.duolingo.ad

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val ClassDef.initializeMonetizationDebugSettingsMethodMatch by ClassDefComposing.composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returnType("V")
    // Parameters have not been reliable for matching between versions.
    opcodes(Opcode.IPUT_BOOLEAN)
}

internal val BytecodePatchContext.monetizationDebugSettingsToStringMethod by gettingFirstMutableMethodDeclaratively {
    name("toString")
    instructions(string("MonetizationDebugSettings(", String::contains))
}
