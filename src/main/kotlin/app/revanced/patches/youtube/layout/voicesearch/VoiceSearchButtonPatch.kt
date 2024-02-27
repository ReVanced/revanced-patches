package app.revanced.patches.youtube.layout.voicesearch

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.patch.voicesearch.AbstractVoiceSearchButtonPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch

@Patch(
    name = "Hide voice search button",
    description = "Hide voice search button in search bar.",
    dependencies = [SettingsPatch::class],
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
    ],
    use = false
)
@Suppress("unused")
object VoiceSearchButtonPatch : AbstractVoiceSearchButtonPatch(
    arrayOf(
        "action_bar_search_results_view_mic.xml",
        "action_bar_search_view.xml",
        "action_bar_search_view_grey.xml",
        "action_bar_search_view_mic_out.xml"
    ),
    arrayOf(
        "height",
        "marginEnd",
        "marginStart",
        "width"
    )
) {
    override fun execute(context: ResourceContext) {
        super.execute(context)

        SettingsPatch.updatePatchStatus("Hide voice search button")
    }
}
