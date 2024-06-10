package app.revanced.patches.reddit.customclients.joeyforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val authUtilityUserAgentFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Ljava/lang/String;")
    opcodes(Opcode.APUT_OBJECT)
    custom { method, classDef ->
        classDef.sourceFile == "AuthUtility.java"
    }
}
