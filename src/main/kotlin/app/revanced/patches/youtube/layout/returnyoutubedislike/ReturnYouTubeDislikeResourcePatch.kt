package app.revanced.patches.youtube.layout.returnyoutubedislike

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.IntentPreference
import app.revanced.patches.youtube.misc.settings.*

internal var oldUIDislikeId = -1L
    private set

internal val returnYouTubeDislikeResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "layout.returnyoutubedislike.ReturnYouTubeDislikeResourcePatch")

        preferences += IntentPreference(
            key = "revanced_settings_screen_09",
            titleKey = "revanced_ryd_settings_title",
            summaryKey = null,
            intent = newIntent("revanced_ryd_settings_intent"),
        )

        oldUIDislikeId = resourceMappings[
            "id",
            "dislike_button",
        ]
    }
}
