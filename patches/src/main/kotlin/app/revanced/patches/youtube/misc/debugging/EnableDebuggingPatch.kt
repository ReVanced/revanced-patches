package app.revanced.patches.youtube.misc.debugging

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.shared.misc.debugging.enableDebuggingPatch
import app.revanced.patches.shared.misc.settings.preference.NonInteractivePreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources

@Suppress("unused")
val enableDebuggingPatch = enableDebuggingPatch(
    block = {
        dependsOn(
            sharedExtensionPatch,
            settingsPatch,
            resourcePatch {
                execute {
                    copyResources(
                        "settings",
                        ResourceGroup("drawable",
                            // Action buttons.
                            "revanced_settings_copy_all.xml",
                            "revanced_settings_deselect_all.xml",
                            "revanced_settings_select_all.xml",
                            // Move buttons.
                            "revanced_settings_arrow_left_double.xml",
                            "revanced_settings_arrow_left_one.xml",
                            "revanced_settings_arrow_right_double.xml",
                            "revanced_settings_arrow_right_one.xml"
                        )
                    )
                }
            }
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
    additionalDebugPreferences = listOf(
        NonInteractivePreference(
            "revanced_debug_feature_flags_manager",
            tag = "app.revanced.extension.youtube.settings.preference.FeatureFlagsManagerPreference",
            selectable = true
        ),
        SwitchPreference("revanced_debug_protobuffer")
    )
)
