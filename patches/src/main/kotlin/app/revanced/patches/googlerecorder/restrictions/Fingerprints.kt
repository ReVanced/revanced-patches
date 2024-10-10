package app.revanced.patches.googlerecorder.restrictions

import app.revanced.patcher.fingerprint

internal val onApplicationCreateFingerprint = fingerprint {
    strings("com.google.android.feature.PIXEL_2017_EXPERIENCE")
    custom { method, classDef ->
        if (method.name != "onCreate") return@custom false

        classDef.endsWith("RecorderApplication;")
    }
}
