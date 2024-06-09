package app.revanced.patches.piccomafr.tracking.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val appMeasurementFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PRIVATE,  AccessFlags.FINAL)
    returns("V")
    strings("config/app/", "Fetching remote configuration")
}