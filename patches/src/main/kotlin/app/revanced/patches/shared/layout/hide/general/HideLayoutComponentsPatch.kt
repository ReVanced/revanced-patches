package app.revanced.patches.shared.layout.hide.general

import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.shared.misc.litho.filter.addLithoFilter
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference

internal fun hideLayoutComponentsPatch(
    lithoFilterPatch: Patch<*>,
    settingsPatch: Patch<*>,
    additionalDependencies: Set<Patch<*>> = emptySet(),
    filterClasses: Set<String>,
    vararg compatibleWithPackages: Pair<String, Set<String>?>,
    executeBlock: BytecodePatchContext.() -> Unit = {},
) = bytecodePatch(
    name = "Hide layout components",
    description = "Adds options to hide general layout components.",
) {
    dependsOn(
        lithoFilterPatch,
        settingsPatch,
        *additionalDependencies.toTypedArray(),
        addResourcesPatch,
    )

    compatibleWith(packages = compatibleWithPackages)

    execute {
        addResources("shared", "layout.hide.general.hideLayoutComponentsPatch")

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

        filterClasses.forEach { className ->
            addLithoFilter(className)
        }

        executeBlock()
    }
}
