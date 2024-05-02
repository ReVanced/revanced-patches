package app.revanced.patches.tiktok.misc.login.fixgoogle.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val googleAuthAvailableFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    custom { methodDef, _ ->
        methodDef.definingClass == "Lcom/bytedance/lobby/google/GoogleAuth;"
    }
}
