package app.revanced.patches.shared.misc.debugging

import app.revanced.patcher.PatchClass
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.BasePreference
import app.revanced.patches.shared.misc.settings.preference.BasePreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference

abstract class BaseDebuggingPatch(
    integrationsPatch: PatchClass,
    settingsPatch: PatchClass,
    compatiblePackages: Set<CompatiblePackage>,
    // TODO: Settings patch should probably be abstracted
    //  so we do not have to pass it in as a dependency AND it's preference screen at the same time.
    private val miscPreferenceScreen: BasePreferenceScreen.Screen,
    private val additionalDebugPreferences: Set<BasePreference> = emptySet(),
    additionalDependencies: Set<PatchClass> = emptySet(),
) : ResourcePatch(
    name = "Enable debugging",
    description = "Adds options for debugging.",
    dependencies = setOf(integrationsPatch, settingsPatch) + AddResourcesPatch::class + additionalDependencies,
    compatiblePackages = compatiblePackages,
) {
    override fun execute(context: ResourceContext) {
        AddResourcesPatch(BaseDebuggingPatch::class)

        miscPreferenceScreen.addPreferences(
            PreferenceScreen(
                "revanced_debug_screen",
                sorting = PreferenceScreen.Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_debug"),
                    SwitchPreference("revanced_debug_stacktrace"),
                    SwitchPreference("revanced_debug_toast_on_error"),
                ) + additionalDebugPreferences,
            ),
        )
    }
}
