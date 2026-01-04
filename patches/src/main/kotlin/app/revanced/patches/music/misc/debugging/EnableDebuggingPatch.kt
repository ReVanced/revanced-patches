package app.revanced.patches.music.misc.debugging

import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.shared.misc.debugging.enableDebuggingPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import kotlin.collections.listOf

@Suppress("unused")
val enableDebuggingPatch = enableDebuggingPatch(
    block = {
        dependsOn(
            sharedExtensionPatch,
            settingsPatch,
        )

        compatibleWith(
            "com.google.android.apps.youtube.music"(
                "7.29.52",
                "8.10.52"
            )
        )
    },
    executeBlock = {
        addResources("shared", "misc.debugging.enableDebuggingPatch")
    },
    // String feature flag does not appear to be present with YT Music.
    hookStringFeatureFlag = false,
    preferenceScreen = PreferenceScreen.MISC,
    additionalDebugPreferences = listOf(SwitchPreference("revanced_debug_protobuffer"))
)
