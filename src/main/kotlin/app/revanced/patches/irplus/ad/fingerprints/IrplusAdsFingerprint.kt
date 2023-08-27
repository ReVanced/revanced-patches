package app.revanced.patches.irplus.ad.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object IrplusAdsFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    listOf("L", "Z"),
    strings = listOf("TAGGED")
)