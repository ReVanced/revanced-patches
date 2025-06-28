package app.revanced.patches.nfctoolsse.misc.pro

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val isLicenseRegisteredFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Z")
    strings("kLicenseCheck")
}