package app.revanced.patches.youtube.video.speed.button

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.youtube.misc.playercontrols.addBottomControls
import app.revanced.patches.youtube.misc.playercontrols.bottomControlsPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources

internal val playbackSpeedButtonResourcePatch = resourcePatch {
    dependsOn(bottomControlsPatch)

    execute { context ->
        context.copyResources(
            "speedbutton",
            ResourceGroup(
                "drawable",
                "revanced_playback_speed_dialog_button.xml",
            ),
        )

        addBottomControls("speedbutton")
    }
}
