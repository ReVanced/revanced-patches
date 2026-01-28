package app.revanced.patches.messenger.metaai

import app.revanced.patcher.*
import com.android.tools.smali.dexlib2.Opcode

internal val getMobileConfigBoolMethodMatch = firstMethodComposite {
    parameterTypes("J")
    returnType("Z")
    opcodes(Opcode.RETURN)
    custom { "Lcom/facebook/mobileconfig/factory/MobileConfigUnsafeContext;" in immutableClassDef.interfaces }
}

internal val metaAIKillSwitchCheckMethodMatch = firstMethodComposite("SearchAiagentImplementationsKillSwitch") {
    opcodes(Opcode.CONST_WIDE)
}

internal val extensionMethodMatch = firstMethodComposite {
    name(EXTENSION_METHOD_NAME)
    definingClass(EXTENSION_CLASS_DESCRIPTOR)
    instructions("REPLACED_BY_PATCH"())
}
