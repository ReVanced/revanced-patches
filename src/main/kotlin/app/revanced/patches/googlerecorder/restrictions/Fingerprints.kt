package app.revanced.patches.googlerecorder.restrictions

import app.revanced.patcher.fingerprint.methodFingerprint

internal val onApplicationCreateFingerprint = methodFingerprint {
    strings("com.google.android.feature.PIXEL_2017_EXPERIENCE")
    custom { methodDef, classDef ->
        if (methodDef.name != "onCreate") return@custom false

        classDef.endsWith("RecorderApplication;")
    }
}
