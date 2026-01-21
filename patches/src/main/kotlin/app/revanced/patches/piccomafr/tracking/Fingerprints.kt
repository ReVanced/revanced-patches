package app.revanced.patches.piccomafr.tracking

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.appMeasurementMethod by gettingFirstMutableMethodDeclaratively("config/app/", "Fetching remote configuration") {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returnType("V")
}

internal val BytecodePatchContext.facebookSDKMethod by gettingFirstMutableMethodDeclaratively("instagram.com", "facebook.com") {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
}

internal val BytecodePatchContext.firebaseInstallMethod by gettingFirstMutableMethodDeclaratively("https://%s/%s/%s", "firebaseinstallations.googleapis.com") {
    accessFlags(AccessFlags.PRIVATE)
}
