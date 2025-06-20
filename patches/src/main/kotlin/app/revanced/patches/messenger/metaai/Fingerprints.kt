package app.revanced.patches.messenger.metaai

import com.android.tools.smali.dexlib2.Opcode
import app.revanced.patcher.fingerprint

internal val getMobileConfigBoolFingerprint by fingerprint {
    parameters("J")
    returns("Z")
    opcodes(Opcode.RETURN)
    custom { _, classDef ->
        classDef.interfaces.contains("Lcom/facebook/mobileconfig/factory/MobileConfigUnsafeContext;")
    }
}

internal val metaAIKillSwitchCheckFingerprint by fingerprint {
    opcodes(Opcode.CONST_WIDE)
    strings("SearchAiagentImplementationsKillSwitch")
}

internal val extensionMethodFingerprint by fingerprint {
    strings("REPLACED_BY_PATCH")
    custom { method, classDef ->
        method.name == EXTENSION_METHOD_NAME && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}
