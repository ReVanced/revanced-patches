package app.revanced.patches.tiktok.misc.login.fixgoogle

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint.methodFingerprint

internal val googleAuthAvailableFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    custom { _, classDef ->
        classDef.type == "Lcom/bytedance/lobby/google/GoogleAuth;"
    }
}

internal val googleOneTapAuthAvailableFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    custom { _, classDef ->
        classDef.type == "Lcom/bytedance/lobby/google/GoogleOneTapAuth;"
    }
}
