package app.revanced.patches.youtube.interaction.copyvideourl

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.playercontrols.*
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.video.information.videoInformationPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources

private val copyVideoUrlResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        playerControlsResourcePatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "interaction.copyvideourl.copyVideoUrlResourcePatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_copy_video_url"),
            SwitchPreference("revanced_copy_video_url_timestamp"),
        )

        copyResources(
            "copyvideourl",
            ResourceGroup(
                resourceDirectoryName = "drawable",
                "revanced_yt_copy.xml",
                "revanced_yt_copy_timestamp.xml",
            ),
        )

        addBottomControl("copyvideourl")
    }
}

@Suppress("unused")
val copyVideoUrlPatch = bytecodePatch(
    name = "Copy video URL",
    description = "Adds options to display buttons in the video player to copy video URLs.",
) {
    dependsOn(
        copyVideoUrlResourcePatch,
        playerControlsPatch,
        videoInformationPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.45.38",
            "19.46.42",
        ),
    )

    execute {
        val extensionPlayerPackage = "Lapp/revanced/extension/youtube/videoplayer"
        val buttonsDescriptors = listOf(
            "$extensionPlayerPackage/CopyVideoUrlButton;",
            "$extensionPlayerPackage/CopyVideoUrlTimestampButton;",
        )

        buttonsDescriptors.forEach { descriptor ->
            initializeBottomControl(descriptor)
            injectVisibilityCheckCall(descriptor)
        }
    }
}
