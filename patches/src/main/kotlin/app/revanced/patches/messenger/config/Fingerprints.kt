package app.revanced.patches.messenger.config

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/messenger/misc/config/OverrideMobileConfigPatch;"

internal val getMobileConfigBoolFingerprint = fingerprint {
    parameters("J")
    returns("Z")
    opcodes(Opcode.RETURN) 
    custom { method, classDef ->
        method.implementation ?: return@custom false  // unsure if this is necessary
        classDef.interfaces.contains("Lcom/facebook/mobileconfig/factory/MobileConfigUnsafeContext;")
    }
}

internal val overrideMobileConfigPatchFingerprint = fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    custom { _, classDef ->
        classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}