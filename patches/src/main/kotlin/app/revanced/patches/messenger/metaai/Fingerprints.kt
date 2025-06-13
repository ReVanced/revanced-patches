package app.revanced.patches.messenger.metaai

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal const val EXTENSION_METHOD_DESCRIPTOR = "Lapp/revanced/extension/messenger/metaai/RemoveMetaAIPatch;->overrideBooleanFlag(JZ)Z"

internal val getMobileConfigBoolFingerprint = fingerprint {
    parameters("J")
    returns("Z")
    opcodes(Opcode.RETURN)
    custom { _, classDef ->
        classDef.interfaces.contains("Lcom/facebook/mobileconfig/factory/MobileConfigUnsafeContext;")
    }
}

internal val relevantIDContainingMethodFingerprint = fingerprint {
    returns("Z")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameters("L", "L", "I")
    strings("SearchAiagentImplementationsKillSwitch")
    opcodes(Opcode.CONST_WIDE)
}

internal val extensionMethodFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameters("J", "Z")
    returns("Z")
    strings("REPLACED_BY_PATCH")
    custom { method, _ ->
        method.toString() == EXTENSION_METHOD_DESCRIPTOR
    }
}
