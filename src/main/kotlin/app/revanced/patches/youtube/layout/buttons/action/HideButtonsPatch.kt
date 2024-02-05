package app.revanced.patches.youtube.layout.buttons.action

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Hide video action buttons",
    description = "Adds options to hide action buttons (such as the Download button) under videos.",
    dependencies = [
        ResourceMappingPatch::class,
        LithoFilterPatch::class,
        AddResourcesPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39",
                "19.03.35",
                "19.03.36",
                "19.04.37"
            ]
        )
    ]
)
@Suppress("unused")
object HideButtonsPatch : ResourcePatch() {
    private const val FILTER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/components/ButtonsFilter;"

    override fun execute(context: ResourceContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                "revanced_hide_buttons_preference_screen",
                preferences = setOf(
                    SwitchPreference("revanced_hide_like_dislike_button"),
                    SwitchPreference("revanced_hide_live_chat_button"),
                    SwitchPreference("revanced_hide_share_button"),
                    SwitchPreference("revanced_hide_report_button"),
                    SwitchPreference("revanced_hide_remix_button"),
                    SwitchPreference("revanced_hide_download_button"),
                    SwitchPreference("revanced_hide_thanks_button"),
                    SwitchPreference("revanced_hide_clip_button"),
                    SwitchPreference("revanced_hide_playlist_button"),
                    SwitchPreference("revanced_hide_shop_button")
                ),
            )
        )

        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)
    }
}
