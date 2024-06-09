package app.revanced.patches.youtube.layout.hide.albumcards

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

internal var albumCardId: Long = -1
    private set

@Suppress("unused")
val albumCardsResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch
    )

    execute {
        addResources("youtube", "layout.hide.albumcards.AlbumCardsResourcePatch")

        PreferenceScreen.FEED.addPreferences(
            SwitchPreference("revanced_hide_album_cards"),
        )

        albumCardId = resourceMappings["layout", "album_card"]
    }
}
