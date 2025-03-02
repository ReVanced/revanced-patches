package app.revanced.patches.piccomafr.misc

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val getAndroidIdFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/String;")
    parameters("Landroid/content/Context;")
    strings("context", "android_id")
}