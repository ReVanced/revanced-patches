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
