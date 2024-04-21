package app.revanced.patches.youtube.ad.general

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    dependencies = [
        LithoFilterPatch::class,
        SettingsPatch::class,
        ResourceMappingPatch::class,
        AddResourcesPatch::class,
    ],
)
object HideAdsResourcePatch : ResourcePatch() {
    private const val FILTER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/components/AdsFilter;"

    internal var adAttributionId: Long = -1

    override fun execute(context: ResourceContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.ADS.addPreferences(
            SwitchPreference("revanced_hide_general_ads"),
            SwitchPreference("revanced_hide_fullscreen_ads"),
            SwitchPreference("revanced_hide_buttoned_ads"),
            SwitchPreference("revanced_hide_paid_promotion_label"),
            SwitchPreference("revanced_hide_self_sponsor_ads"),
            SwitchPreference("revanced_hide_products_banner"),
            SwitchPreference("revanced_hide_shopping_links"),
            SwitchPreference("revanced_hide_web_search_results"),
            SwitchPreference("revanced_hide_merchandise_banners"),
        )

        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)

        adAttributionId = ResourceMappingPatch["id", "ad_attribution"]
    }
}
