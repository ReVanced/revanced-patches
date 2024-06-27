package app.revanced.patches.soundcloud.analytics.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object CreateTrackingApiFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC.value,
    customFingerprint = { methodDef, classDef ->
        classDef.sourceFile == "DefaultTrackingApiFactory.kt" && methodDef.name == "create"
    },
)
