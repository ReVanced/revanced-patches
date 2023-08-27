package app.revanced.patches.youtube.ad.general.resource.patch

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.ad.general.annotation.HideAdsCompatibility
import app.revanced.patches.youtube.misc.litho.filter.patch.LithoFilterPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch.PreferenceScreen

@DependsOn(
    [
        LithoFilterPatch::class,
        SettingsPatch::class,
        ResourceMappingPatch::class
    ]
)
@HideAdsCompatibility
class HideAdsResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext) {
        PreferenceScreen.ADS.addPreferences(
            SwitchPreference(
                "revanced_hide_general_ads",
                StringResource("revanced_hide_general_ads_title", "Hide general ads"),
                StringResource("revanced_hide_general_ads_summary_on", "General ads are hidden"),
                StringResource("revanced_hide_general_ads_summary_off", "General ads are shown")
            ),
            SwitchPreference(
                "revanced_hide_buttoned_ads",
                StringResource("revanced_hide_buttoned_ads_title", "Hide buttoned ad"),
                StringResource("revanced_hide_buttoned_ads_summary_on", "Buttoned ads are hidden"),
                StringResource("revanced_hide_buttoned_ads_summary_off", "Buttoned ads are shown")
            ),
            SwitchPreference(
                "revanced_hide_paid_content_ads",
                StringResource("revanced_hide_paid_content_ads_title", "Hide paid content"),
                StringResource("revanced_hide_paid_content_ads_summary_on", "Paid content is hidden"),
                StringResource("revanced_hide_paid_content_ads_summary_off", "Paid content is shown")
            ),
            SwitchPreference(
                "revanced_hide_self_sponsor_ads",
                StringResource("revanced_hide_self_sponsor_ads_title", "Hide self sponsored cards"),
                StringResource("revanced_hide_self_sponsor_ads_summary_on", "Self sponsored cards are hidden"),
                StringResource("revanced_hide_self_sponsor_ads_summary_off", "Self sponsored cards are shown")
            ),
            SwitchPreference(
                "revanced_hide_products_banner",
                StringResource("revanced_hide_products_banner_title", "Hide banner to view products"),
                StringResource("revanced_hide_products_banner_summary_on", "Banner is hidden"),
                StringResource("revanced_hide_products_banner_summary_off", "Banner is shown")
            ),
            SwitchPreference(
                "revanced_hide_web_search_results",
                StringResource("revanced_hide_web_search_results_title", "Hide web search results"),
                StringResource("revanced_hide_web_search_results_summary_on", "Web search results are hidden"),
                StringResource("revanced_hide_web_search_results_summary_off", "Web search results are shown")
            ),
            SwitchPreference(
                "revanced_hide_merchandise_banners",
                StringResource("revanced_hide_merchandise_banners_title", "Hide merchandise banners"),
                StringResource("revanced_hide_merchandise_banners_summary_on", "Merchandise banners are hidden"),
                StringResource("revanced_hide_merchandise_banners_summary_off", "Merchandise banners are shown")
            )
        )

        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)

        adAttributionId = ResourceMappingPatch.resourceMappings.single { it.name == "ad_attribution" }.id
    }

    internal companion object {
        var adAttributionId: Long = -1

        private const val FILTER_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/components/AdsFilter;"
    }
}
