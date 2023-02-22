package app.revanced.patches.youtube.layout.hide.breakingnews.resource.patch

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch

@Name("breaking-news-shelf-resource-patch")
@DependsOn([SettingsPatch::class, ResourceMappingPatch::class])
@Version("0.0.1")
class BreakingNewsResourcePatch : ResourcePatch {
    companion object {
        internal var horizontalCardListId: Long = -1
    }

    override fun execute(context: ResourceContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_breaking_news",
                StringResource("revanced_hide_breaking_news_title", "Hide breaking news"),
                true,
                StringResource("revanced_hide_breaking_news_summary_on", "Breaking news are hidden"),
                StringResource("revanced_hide_breaking_news_summary_off", "Breaking news are shown")
            )
        )

        horizontalCardListId = ResourceMappingPatch.resourceMappings.single {
            it.type == "layout" && it.name == "horizontal_card_list"
        }.id

        return PatchResultSuccess()
    }
}