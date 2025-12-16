package app.revanced.patches.music.layout.hide.general

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.music.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.shared.misc.litho.filter.addLithoFilter
import app.revanced.patches.shared.misc.settings.preference.*

private const val CUSTOM_FILTER_CLASS_NAME = "Lapp/revanced/extension/shared/patches/components/CustomFilter;"

val hideLayoutComponentsPatch = bytecodePatch(
    name = "Hide layout components",
    description = "Adds options to hide general layout components.",

    ) {
    dependsOn(
        lithoFilterPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "7.29.52", "8.10.52"
        )
    )

    execute {
        // TODO: Move Resources to music/shared specific
        addResources("youtube", "layout.hide.general.hideLayoutComponentsPatch")

        PreferenceScreen.GENERAL.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_custom_filter_screen",
                sorting = PreferenceScreenPreference.Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_custom_filter"),
                    TextPreference("revanced_custom_filter_strings", inputType = InputType.TEXT_MULTI_LINE),
                ),
            ),
        )

        addLithoFilter(CUSTOM_FILTER_CLASS_NAME)
    }
}
