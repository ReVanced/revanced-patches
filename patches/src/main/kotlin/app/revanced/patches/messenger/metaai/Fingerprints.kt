package app.revanced.patches.messenger.metaai

import com.android.tools.smali.dexlib2.Opcode
import app.revanced.patcher.fingerprint

internal val getMobileConfigBoolFingerprint by fingerprint {
    parameters("J")
    returns("Z")
    opcodes(Opcode.RETURN) 
    custom { method, classDef ->
        method.implementation ?: return@custom false  // unsure if this is necessary
        classDef.interfaces.contains("Lcom/facebook/mobileconfig/factory/MobileConfigUnsafeContext;")
    }
}