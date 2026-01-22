package app.revanced.patches.messenger.metaai

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.getMobileConfigBoolMethod by gettingFirstMutableMethodDeclaratively {
    returnType("Z")
    opcodes(Opcode.RETURN)
    custom { immutableClassDef.interfaces.contains("Lcom/facebook/mobileconfig/factory/MobileConfigUnsafeContext;") }
}

internal val BytecodePatchContext.metaAIKillSwitchCheckMethod by gettingFirstMutableMethodDeclaratively("SearchAiagentImplementationsKillSwitch") {
    opcodes(Opcode.CONST_WIDE)

}
internal val BytecodePatchContext.extensionMethodMethod by gettingFirstMutableMethodDeclaratively("REPLACED_BY_PATCH") {
    name(EXTENSION_METHOD_NAME)
    definingClass(EXTENSION_CLASS_DESCRIPTOR)
}
