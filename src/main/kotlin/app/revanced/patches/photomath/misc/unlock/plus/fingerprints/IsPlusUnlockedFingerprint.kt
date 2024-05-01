package app.revanced.patches.photomath.misc.unlock.plus.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val isPlusUnlockedFingerprint = methodFingerprint{
    returns("Z")
    accessFlags(AccessFlags.PUBLIC,AccessFlags.FINAL)
    strings(
        "genius"
    )
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("/User;")
    }
}