package app.revanced.patches.youtube.ad.general

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch.PreferenceScreen
import app.revanced.patches.youtube.misc.strings.StringsPatch

@Patch(
    dependencies = [
        LithoFilterPatch::class,
        SettingsPatch::class,
        ResourceMappingPatch::class
    ]
)
object HideAdsResourcePatch : ResourcePatch() {
    private const val FILTER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/patches/components/AdsFilter;"

    internal var adAttributionId: Long = -1

    override fun execute(context: ResourceContext) {
        StringsPatch.includePatchStrings("HideAds")
        PreferenceScreen.ADS.addPreferences(
            SwitchPreference(
                "revanced_hide_general_ads",
                "revanced_hide_general_ads_title",
                "revanced_hide_general_ads_summary_on",
                "revanced_hide_general_ads_summary_off"
            ),
            SwitchPreference(
                "revanced_hide_buttoned_ads",
                "revanced_hide_buttoned_ads_title",
                "revanced_hide_buttoned_ads_summary_on",
                "revanced_hide_buttoned_ads_summary_off"
            ),
            SwitchPreference(
                "revanced_hide_paid_content_ads",
                "revanced_hide_paid_content_ads_title",
                "revanced_hide_paid_content_ads_summary_on",
                "revanced_hide_paid_content_ads_summary_off"
            ),
            SwitchPreference(
                "revanced_hide_self_sponsor_ads",
                "revanced_hide_self_sponsor_ads_title",
                "revanced_hide_self_sponsor_ads_summary_on",
                "revanced_hide_self_sponsor_ads_summary_off"
            ),
            SwitchPreference(
                "revanced_hide_products_banner",
                "revanced_hide_products_banner_title",
                "revanced_hide_products_banner_summary_on",
                "revanced_hide_products_banner_summary_off"
            ),
            SwitchPreference(
                "revanced_hide_shopping_links",
                "revanced_hide_shopping_links_title",
                "revanced_hide_shopping_links_summary_on",
                "revanced_hide_shopping_links_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_web_search_results",
                "revanced_hide_web_search_results_title",
                "revanced_hide_web_search_results_summary_on",
                "revanced_hide_web_search_results_summary_off",
            ),
            SwitchPreference(
                "revanced_hide_merchandise_banners",
                "revanced_hide_merchandise_banners_title",
                "revanced_hide_merchandise_banners_summary_on",
                "revanced_hide_merchandise_banners_summary_off",
            )
        )

        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)

        adAttributionId =
            ResourceMappingPatch.resourceMappings.single { it.name == "ad_attribution" }.id

    }
}
