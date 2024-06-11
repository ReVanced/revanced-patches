package app.revanced.patches.nfctoolsse.misc.pro

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint.methodFingerprint

internal val isLicenseRegisteredFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Z")
    strings("kLicenseCheck")
}