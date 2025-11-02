package app.revanced.patches.youtube.misc.debugging

import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.shared.misc.debugging.enableDebuggingPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

@Suppress("unused")
val enableDebuggingPatch = enableDebuggingPatch(
    block = {
        dependsOn(
            sharedExtensionPatch,
            settingsPatch,
        )

        compatibleWith(
            "com.google.android.youtube"(
                "19.34.42",
                "20.07.39",
                "20.13.41",
                "20.14.43",
            )
        )
    },
    executeBlock = {
        addResources("youtube", "misc.debugging.enableDebuggingPatch")
    },
    hookStringFeatureFlag = true,
    preferenceScreen = PreferenceScreen.MISC,
    additionalDebugPreferences = listOf(SwitchPreference("revanced_debug_protobuffer"))
)
