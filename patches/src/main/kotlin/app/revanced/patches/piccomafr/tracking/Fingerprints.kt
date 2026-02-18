package app.revanced.patches.piccomafr.tracking

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.appMeasurementMethod by gettingFirstMethodDeclaratively("config/app/", "Fetching remote configuration") {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returnType("V")
}

internal val BytecodePatchContext.facebookSDKMethod by gettingFirstMethodDeclaratively("instagram.com", "facebook.com") {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
}

internal val BytecodePatchContext.firebaseInstallMethod by gettingFirstMethodDeclaratively("https://%s/%s/%s", "firebaseinstallations.googleapis.com") {
    accessFlags(AccessFlags.PRIVATE)
}
