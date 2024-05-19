package app.revanced.patches.piccomafr.misc.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags


internal object GetAndroidIDFingerprint : MethodFingerprint(
    parameters = listOf("Landroid/content/Context"),
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    strings = listOf(
        "context",
        "android_id"
    ),
    returnType = "Ljava/lang/String"
)