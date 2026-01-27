package app.revanced.patches.music.shared

import app.revanced.patcher.fingerprint

internal const val YOUTUBE_MUSIC_MAIN_ACTIVITY_CLASS_TYPE = "Lcom/google/android/apps/youtube/music/activities/MusicActivity;"

internal val mainActivityOnCreateFingerprint = fingerprint {
    returns("V")
    parameters("Landroid/os/Bundle;")
    custom { method, classDef ->
        method.name == "onCreate" && classDef.type == YOUTUBE_MUSIC_MAIN_ACTIVITY_CLASS_TYPE
    }
}

internal val conversionContextFingerprintToString = fingerprint {
    parameters()
    strings(
        "ConversionContext{containerInternal=",
        ", gridColumnCount=",
        ", gridColumnIndex=",
        ", templateLoggerFactory=",
        ", rootDisposableContainer=",
        ", elementId=",
        ", identifierProperty="
    )
    custom { method, _ ->
        method.name == "toString"
    }
}
