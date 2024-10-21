package app.revanced.patches.all.privacy.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// Matches com.appsflyer.internal.AFb1vSDK.init.
internal object InitAppsFlyerSDKFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L", "L", "L"),
    strings = listOf("Initializing AppsFlyer SDK: (v%s.%s)"),
)
