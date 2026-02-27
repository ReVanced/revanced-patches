package app.revanced.patches.angulus.ads

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

// Keywords to search for in case the method name changes:
//     dailyMeasurementCount
//     lastMeasurementDate
//     dailyAdResetCount
//     MeasurementPrefs

// This targets a method that returns the daily measurement count.
// This method is used to determine if the user has reached the daily limit of measurements.
internal val BytecodePatchContext.getDailyMeasurementCountMethod by gettingFirstMethodDeclaratively("dailyMeasurementCount") {
    accessFlags(AccessFlags.PRIVATE)
    returnType("I")
}
