package app.revanced.patches.willhaben.ads.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object AdResolverFingerprint : MethodFingerprint(
    "L",
    parameters = listOf("L", "L"),
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    strings = listOf(
        "Google Ad is invalid ",
        "Google Native Ad is invalid ",
        "Criteo Ad is invalid ",
        "Amazon Ad is invalid "
    )
)
