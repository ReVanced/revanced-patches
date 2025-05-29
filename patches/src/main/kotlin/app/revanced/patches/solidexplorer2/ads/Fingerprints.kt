package app.revanced.patches.solidexplorer2.ads

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val checkLicenceOnBackendFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    parameters("Ljava/lang/String;")
    returns("Z")
    strings("0")
}