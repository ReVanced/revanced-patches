package app.revanced.patches.nfctoolsse.misc.pro

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

@Deprecated("This patch no longer works and will soon be deleted.")
internal val isLicenseRegisteredFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Z")
    strings("kLicenseCheck")
}