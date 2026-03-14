package app.revanced.patches.messenger.metaai

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.getMobileConfigBoolMethodMatch by composingFirstMethod {
    parameterTypes("J")
    returnType("Z")
    opcodes(Opcode.RETURN)
    custom { "Lcom/facebook/mobileconfig/factory/MobileConfigUnsafeContext;" in immutableClassDef.interfaces }
}

internal val BytecodePatchContext.metaAIKillSwitchCheckMethodMatch by composingFirstMethod {
    opcodes(Opcode.CONST_WIDE)
    instructions("SearchAiagentImplementationsKillSwitch"(String::contains))
}

internal val BytecodePatchContext.extensionMethodMatch by composingFirstMethod {
    name(EXTENSION_METHOD_NAME)
    definingClass(EXTENSION_CLASS_DESCRIPTOR)
    instructions("REPLACED_BY_PATCH"())
}
