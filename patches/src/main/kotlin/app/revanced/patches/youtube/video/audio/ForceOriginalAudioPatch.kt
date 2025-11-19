package app.revanced.patches.youtube.video.audio

import app.revanced.patches.shared.misc.audio.forceOriginalAudioPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_20_07_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.mainActivityOnCreateFingerprint

@Suppress("unused")
val forceOriginalAudioPatch = forceOriginalAudioPatch(
    block = {
        dependsOn(
            sharedExtensionPatch,
            settingsPatch,
            versionCheckPatch
        )

        compatibleWith(
            "com.google.android.youtube"(
                "19.43.41",
                "20.14.43",
                "20.21.37",
                "20.31.40",
            )
        )
    },
    fixUseLocalizedAudioTrackFlag = { is_20_07_or_greater },
    mainActivityOnCreateFingerprint = mainActivityOnCreateFingerprint,
    subclassExtensionClassDescriptor = "Lapp/revanced/extension/youtube/patches/ForceOriginalAudioPatch;",
    preferenceScreen = PreferenceScreen.VIDEO,
)
