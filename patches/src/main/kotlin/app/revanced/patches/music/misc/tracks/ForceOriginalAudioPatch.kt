package app.revanced.patches.music.misc.tracks

import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.music.playservice.is_8_05_or_greater
import app.revanced.patches.music.playservice.versionCheckPatch
import app.revanced.patches.music.shared.mainActivityOnCreateFingerprint
import app.revanced.patches.shared.misc.audio.forceOriginalAudioPatch

@Suppress("unused")
val forceOriginalAudioPatch = forceOriginalAudioPatch(
    block = {
        dependsOn(
            sharedExtensionPatch,
            settingsPatch,
            versionCheckPatch
        )

        compatibleWith(
            "com.google.android.apps.youtube.music"(
                "7.29.52",
                "8.10.52"
            )
        )
    },
    fixUseLocalizedAudioTrackFlag = { is_8_05_or_greater },
    mainActivityOnCreateFingerprint = mainActivityOnCreateFingerprint,
    subclassExtensionClassDescriptor = "Lapp/revanced/extension/music/patches/ForceOriginalAudioPatch;",
    preferenceScreen = PreferenceScreen.MISC,
)
