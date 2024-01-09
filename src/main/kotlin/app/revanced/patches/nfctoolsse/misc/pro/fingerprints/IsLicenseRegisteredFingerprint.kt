package app.revanced.patches.nfctoolsse.misc.pro.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object IsLicenseRegisteredFingerprint : MethodFingerprint(
    returnType = "Z",
    accessFlags = AccessFlags.PUBLIC.value,
    strings = listOf("kLicenseCheck")
)