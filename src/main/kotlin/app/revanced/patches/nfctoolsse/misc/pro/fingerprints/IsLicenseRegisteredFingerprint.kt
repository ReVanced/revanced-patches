package app.revanced.patches.nfctoolsse.misc.pro.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val isLicenseRegisteredFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Z")
    strings("kLicenseCheck")
}