package app.revanced.patches.all.privacy.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object InitSDKFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PRIVATE.value,
    parameters = listOf("L", "L"),
    strings = listOf("manualStart", "afDevKey", "AF Dev Key is empty"),
)
