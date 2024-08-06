package app.revanced.patches.all.privacy.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// Original method path is com.appsflyer.internal.AFb1vSDK.init
internal object AppsFlyerSDKInitFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L", "L", "L"),
    strings = listOf("Initializing AppsFlyer SDK: (v%s.%s)")
)