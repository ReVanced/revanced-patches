package app.revanced.patches.all.analytics.segment.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object SegmentBuilderFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    parameters = listOf("Landroid/content/Context;", "Ljava/lang/String;"),
    strings = listOf("writeKey must not be empty.")
)