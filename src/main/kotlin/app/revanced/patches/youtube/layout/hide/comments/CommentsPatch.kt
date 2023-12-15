package app.revanced.patches.youtube.layout.hide.comments

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.strings.StringsPatch
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
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.41",
                "18.45.43"
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

        StringsPatch.includePatchStrings("Comments")
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                "revanced_comments_preference_screen",
                "revanced_comments_preference_screen_title",
                listOf(
                    SwitchPreference(
                        "revanced_hide_comments_section",
                        "revanced_hide_comments_section_title",
                        "revanced_hide_comments_section_summary_on",
                        "revanced_hide_comments_section_summary_off"
                    ),
                    SwitchPreference(
                        "revanced_hide_preview_comment",
                        "revanced_hide_preview_comment_title",
                        "revanced_hide_preview_comment_on",
                        "revanced_hide_preview_comment_off"
                    )
                ),
                "revanced_comments_preference_screen_summary"
            )
        )
    }
}
