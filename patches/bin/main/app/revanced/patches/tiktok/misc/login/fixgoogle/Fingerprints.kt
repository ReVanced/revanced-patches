package app.revanced.patches.tiktok.misc.login.fixgoogle

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val googleAuthAvailableFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    custom { method, _ ->
        method.definingClass == "Lcom/bytedance/lobby/google/GoogleAuth;"
    }
}

internal val googleOneTapAuthAvailableFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    custom { method, _ ->
        method.definingClass == "Lcom/bytedance/lobby/google/GoogleOneTapAuth;"
    }
}
