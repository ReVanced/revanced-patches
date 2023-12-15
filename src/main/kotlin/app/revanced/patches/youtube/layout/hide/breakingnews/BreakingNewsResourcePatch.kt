package app.revanced.patches.youtube.layout.hide.breakingnews

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.strings.StringsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    dependencies = [
        SettingsPatch::class,
        ResourceMappingPatch::class
    ],
)
internal object BreakingNewsResourcePatch : ResourcePatch() {
    internal var horizontalCardListId: Long = -1

    override fun execute(context: ResourceContext) {
        StringsPatch.includePatchStrings("BreakingNews")
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_breaking_news",
                "revanced_hide_breaking_news_title",
                "revanced_hide_breaking_news_summary_on",
                "revanced_hide_breaking_news_summary_off"
            )
        )

        horizontalCardListId = ResourceMappingPatch.resourceMappings.single {
            it.type == "layout" && it.name == "horizontal_card_list"
        }.id
    }
}