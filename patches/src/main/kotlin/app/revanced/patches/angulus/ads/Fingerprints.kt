package app.revanced.patches.angulus.ads

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// Keywords to search for in case the method name changes:
//     dailyMeasurementCount
//     lastMeasurementDate
//     dailyAdResetCount
//     MeasurementPrefs

// This fingerprint targets a method that returns the daily measurement count.
// This method is used to determine if the user has reached the daily limit of measurements.
internal val getDailyMeasurementCountFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE)
    returns("I")
    strings("dailyMeasurementCount")
}
