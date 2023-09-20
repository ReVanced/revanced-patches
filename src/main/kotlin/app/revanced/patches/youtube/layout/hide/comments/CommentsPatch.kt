package app.revanced.patches.youtube.layout.hide.comments

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Comments",
    description = "Hides components related to comments.",
    dependencies = [
        SettingsPatch::class,
        LithoFilterPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.16.37",
                "18.19.35",
                "18.20.39",
                "18.23.35",
                "18.29.38",
                "18.32.39"
            ]
        )
    ]
)
@Suppress("unused")
object CommentsPatch : ResourcePatch() {
    private const val FILTER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/patches/components/CommentsFilter;"

    override fun execute(context: ResourceContext) {
        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)
        
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                "revanced_comments_preference_screen",
                StringResource("revanced_comments_preference_screen_title", "Comments"),
                listOf(
                    SwitchPreference(
                        "revanced_hide_comments_section",
                        StringResource("revanced_hide_comments_section_title", "Hide comments section"),
                        StringResource("revanced_hide_comments_section_summary_on", "Comment section is hidden"),
                        StringResource("revanced_hide_comments_section_summary_off", "Comment section is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_preview_comment",
                        StringResource("revanced_hide_preview_comment_title", "Hide preview comment"),
                        StringResource("revanced_hide_preview_comment_on", "Preview comment is hidden"),
                        StringResource("revanced_hide_preview_comment_off", "Preview comment is shown")
                    )
                ),
                StringResource("revanced_comments_preference_screen_summary", "Manage the visibility of comments section components")
            )
        )
    }
}
