package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object SubtitleWindowSettingsConstructorFingerprint : MethodFingerprint(
    parameters = listOf("I", "I", "I", "Z", "Z"),
    customFingerprint = { methodDef ->
        methodDef.definingClass == "Lcom/google/android/libraries/youtube/player/subtitles/model/SubtitleWindowSettings;"
                && methodDef.name == "<init>"
    }
)