package app.revanced.patches.youtube.layout.hide.comments

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Comments",
    description = "Adds options to hide components related to comments.",
    dependencies = [
        SettingsPatch::class,
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
                "19.03.36",
                "19.04.38",
                "19.05.36",
                "19.06.39",
                "19.07.40",
                "19.08.36",
                "19.09.38",
                "19.10.39",
                "19.11.43",
                "19.12.41",
                "19.13.37",
                "19.14.43",
                "19.15.36",
                "19.16.39",
            ]
        )
    ]
)
@Suppress("unused")
object CommentsPatch : ResourcePatch() {
    private const val FILTER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/components/CommentsFilter;"

    override fun execute(context: ResourceContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.PLAYER.addPreferences(
            PreferenceScreen(
                "revanced_comments_screen",
                preferences = setOf(
                    SwitchPreference("revanced_hide_comments_by_members_header"),
                    SwitchPreference("revanced_hide_comments_section"),
                    SwitchPreference("revanced_hide_comments_preview_comment"),
                    SwitchPreference("revanced_hide_comments_thanks_button"),
                    SwitchPreference("revanced_hide_comments_timestamp_and_emoji_buttons")
                ),
                sorting = PreferenceScreen.Sorting.UNSORTED
            )
        )

        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)
    }
}
