package app.revanced.patches.youtube.layout.hide.crowdfundingbox

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

var crowdfundingBoxId = -1L

val crowdfundingBoxResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "layout.hide.crowdfundingbox.CrowdfundingBoxResourcePatch")

        PreferenceScreen.FEED.addPreferences(
            SwitchPreference("revanced_hide_crowdfunding_box"),
        )

        crowdfundingBoxId = resourceMappings[
            "layout",
            "donation_companion",
        ]
    }
}
