package app.revanced.patches.music.misc.spoof

import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.gms.musicActivityOnCreateFingerprint
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.music.playservice.is_7_33_or_greater
import app.revanced.patches.music.playservice.is_8_11_or_greater
import app.revanced.patches.music.playservice.is_8_15_or_greater
import app.revanced.patches.music.playservice.versionCheckPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.spoof.spoofVideoStreamsPatch

val spoofVideoStreamsPatch = spoofVideoStreamsPatch(
    extensionClassDescriptor = "Lapp/revanced/extension/music/patches/spoof/SpoofVideoStreamsPatch;",
    mainActivityOnCreateFingerprint = musicActivityOnCreateFingerprint,
    fixMediaFetchHotConfig = { true },
    fixMediaFetchHotConfigAlternative = { is_8_11_or_greater && !is_8_15_or_greater },
    fixParsePlaybackResponseFeatureFlag = { is_7_33_or_greater },

    block = {
        dependsOn(
            sharedExtensionPatch,
            settingsPatch,
            addResourcesPatch,
            versionCheckPatch,
            userAgentClientSpoofPatch
        )

        compatibleWith(
            "com.google.android.apps.youtube.music"(
                "7.29.52",
                "8.10.52"
            )
        )
    },

    executeBlock = {
        addResources("music", "misc.fix.playback.spoofVideoStreamsPatch")

        PreferenceScreen.MISC.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_spoof_video_streams_screen",
                sorting = PreferenceScreenPreference.Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_spoof_video_streams"),
                    ListPreference("revanced_spoof_video_streams_client_type"),
                )
            )
        )
    }
)
