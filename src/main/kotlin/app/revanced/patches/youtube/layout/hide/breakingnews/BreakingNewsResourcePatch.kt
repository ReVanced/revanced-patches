package app.revanced.patches.youtube.layout.hide.breakingnews

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    dependencies = [
        SettingsPatch::class,
        ResourceMappingPatch::class
    ],
)
object BreakingNewsResourcePatch : ResourcePatch() {
    internal var horizontalCardListId: Long = -1

    override fun execute(context: ResourceContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_breaking_news",
                StringResource("revanced_hide_breaking_news_title", "Hide breaking news"),
                StringResource("revanced_hide_breaking_news_summary_on", "Breaking news are hidden"),
                StringResource("revanced_hide_breaking_news_summary_off", "Breaking news are shown")
            )
        )

        horizontalCardListId = ResourceMappingPatch.resourceMappings.single {
            it.type == "layout" && it.name == "horizontal_card_list"
        }.id
    }
}