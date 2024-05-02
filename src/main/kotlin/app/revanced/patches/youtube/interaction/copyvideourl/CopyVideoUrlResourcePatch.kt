package app.revanced.patches.youtube.interaction.copyvideourl

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.playercontrols.BottomControlsResourcePatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources

@Suppress("unused")
val copyVideoUrlResourcePatch = resourcePatch {
    dependsOn(settingsPatch, BottomControlsResourcePatch, addResourcesPatch)

    execute { context ->
        addResources("youtube", "interaction.copyvideourl.CopyVideoUrlResourcePatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_copy_video_url"),
            SwitchPreference("revanced_copy_video_url_timestamp"),
        )

        context.copyResources(
            "copyvideourl",
            ResourceGroup(
                resourceDirectoryName = "drawable",
                "revanced_yt_copy.xml",
                "revanced_yt_copy_timestamp.xml",
            ),
        )

        BottomControlsResourcePatch.addControls("copyvideourl")
    }
}
