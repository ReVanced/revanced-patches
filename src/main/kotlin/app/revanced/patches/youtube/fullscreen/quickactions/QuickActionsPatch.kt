package app.revanced.patches.youtube.fullscreen.quickactions

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.utils.integrations.Constants.COMPONENTS_PATH
import app.revanced.patches.youtube.utils.litho.LithoFilterPatch
import app.revanced.patches.youtube.utils.quickactions.QuickActionsHookPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch

@Patch(
    name = "Quick actions components",
    description = "Adds options to hide and customize components below the seekbar in fullscreen.",
    dependencies = [
        LithoFilterPatch::class,
        QuickActionsHookPatch::class,
        SettingsPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object QuickActionsPatch : BytecodePatch(emptySet()) {
    override fun execute(context: BytecodeContext) {
        LithoFilterPatch.addFilter("$COMPONENTS_PATH/QuickActionFilter;")

        QuickActionsHookPatch.injectQuickActionMargin()

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: BOTTOM_PLAYER_SETTINGS",
                "SETTINGS: QUICK_ACTIONS_COMPONENTS"
            )
        )

        SettingsPatch.updatePatchStatus("Quick actions components")

    }
}
