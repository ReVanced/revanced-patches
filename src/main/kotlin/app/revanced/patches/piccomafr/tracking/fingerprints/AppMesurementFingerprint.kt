package app.revanced.patches.piccomafr.tracking.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags


internal object AppMesurementFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PRIVATE or AccessFlags.FINAL,
    strings = listOf(
        "config/app/",
        "Fetching remote configuration"
    ),
    returnType = "V"
)