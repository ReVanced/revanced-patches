package app.revanced.patches.googlerecorder.restrictions.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object OnApplicationCreateFingerprint : MethodFingerprint(
    strings = listOf("com.google.android.feature.PIXEL_2017_EXPERIENCE"),
    customFingerprint = custom@{ methodDef, classDef ->
        if (methodDef.name != "onCreate") return@custom false

        classDef.type.endsWith("RecorderApplication;")
    }
)
