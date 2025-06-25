package app.revanced.patches.messenger.metaai

import com.android.tools.smali.dexlib2.Opcode
import app.revanced.patcher.fingerprint

internal val getMobileConfigBoolFingerprint = fingerprint {
    parameters("J")
    returns("Z")
    opcodes(Opcode.RETURN)
    custom { _, classDef ->
        classDef.interfaces.contains("Lcom/facebook/mobileconfig/factory/MobileConfigUnsafeContext;")
    }
}

internal val metaAIKillSwitchCheckFingerprint = fingerprint {
    strings("SearchAiagentImplementationsKillSwitch")
    opcodes(Opcode.CONST_WIDE)
}

internal val extensionMethodFingerprint = fingerprint {
    strings("REPLACED_BY_PATCH")
    custom { method, classDef ->
        method.name == EXTENSION_METHOD_NAME && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}
