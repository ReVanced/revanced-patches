package app.revanced.patches.messenger.config

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/messenger/config/AppFeatureFlagsPatch;"

internal val getMobileConfigBoolFingerprint = fingerprint {
    parameters("J")
    returns("Z")
    opcodes(Opcode.RETURN) 
    custom { _, classDef ->
        classDef.interfaces.contains("Lcom/facebook/mobileconfig/factory/MobileConfigUnsafeContext;")
    }
}

internal val appFeatureFlagsPatchFingerprint = fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    custom { _, classDef ->
        classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}
