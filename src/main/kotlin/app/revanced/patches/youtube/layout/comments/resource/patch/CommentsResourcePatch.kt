package app.revanced.patches.youtube.layout.comments.resource.patch

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.youtube.layout.comments.annotations.CommentsCompatibility
import app.revanced.patches.youtube.misc.mapping.patch.ResourceMappingPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference

@Name("comments-resource-patch")
@CommentsCompatibility
@DependsOn([SettingsPatch::class, ResourceMappingPatch::class])
@Version("0.0.1")
class CommentsResourcePatch : ResourcePatch {
    companion object {
        internal var shortsCommentsButtonId: Long = -1
    }

    override fun execute(context: ResourceContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                "revanced_comments",
                StringResource("revanced_comments_title", "Comments"),
                listOf(
                    SwitchPreference(
                        "revanced_hide_comments_section",
                        StringResource("revanced_hide_comments_section_title", "Remove comments section"),
                        false,
                        StringResource("revanced_hide_comments_section_summary_on", "Comment section is hidden"),
                        StringResource("revanced_hide_comments_section_summary_off", "Comment section is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_preview_comment",
                        StringResource("revanced_hide_preview_comment_title", "Hide preview comment"),
                        false,
                        StringResource("revanced_hide_preview_comment_on", "Preview comment is hidden"),
                        StringResource("revanced_hide_preview_comment_off", "Preview comment is shown")
                    ),
                    SwitchPreference(
                        "revanced_hide_shorts_comments_button",
                        StringResource("revanced_hide_shorts_comments_button_title", "Hide shorts comments button"),
                        false,
                        StringResource("revanced_hide_shorts_comments_button_on", "Shorts comments button is hidden"),
                        StringResource("revanced_hide_shorts_comments_button_off", "Shorts comments button is shown")
                    ),
                ),
                StringResource("revanced_comments_summary", "Manage the visibility of comments section components")
            )
        )

        shortsCommentsButtonId = ResourceMappingPatch.resourceMappings.single {
            it.type == "drawable" && it.name == "ic_right_comment_32c"
        }.id

        return PatchResultSuccess()
    }
}