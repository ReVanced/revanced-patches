package app.revanced.patches.photomath.misc.unlock.plus.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object IsPlusUnlockedFingerprint : MethodFingerprint(
    returnType = "Z",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    strings = listOf(
        "genius"
    ),
    customFingerprint = {
        methodDef, _ -> methodDef.definingClass.endsWith("/User;")
    }
)