package app.revanced.patches.youtube.ad.general

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.litho.filter.addFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

internal var adAttributionId: Long = -1
    private set

@Suppress("unused")
val hideAdsResourcePatch = resourcePatch {
    dependsOn(
        lithoFilterPatch,
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
    )

    val filterClassDescriptor =
        "Lapp/revanced/integrations/youtube/patches/components/AdsFilter;"

    execute {
        addResources("youtube", "ad.general.HideAdsResourcePatch")

        PreferenceScreen.ADS.addPreferences(
            SwitchPreference("revanced_hide_general_ads"),
            SwitchPreference("revanced_hide_fullscreen_ads"),
            SwitchPreference("revanced_hide_buttoned_ads"),
            SwitchPreference("revanced_hide_paid_promotion_label"),
            SwitchPreference("revanced_hide_self_sponsor_ads"),
            SwitchPreference("revanced_hide_products_banner"),
            SwitchPreference("revanced_hide_shopping_links"),
            SwitchPreference("revanced_hide_visit_store_button"),
            SwitchPreference("revanced_hide_web_search_results"),
            SwitchPreference("revanced_hide_merchandise_banners"),
        )

        addFilter(filterClassDescriptor)

        adAttributionId = resourceMappings["id", "ad_attribution"]
    }
}
