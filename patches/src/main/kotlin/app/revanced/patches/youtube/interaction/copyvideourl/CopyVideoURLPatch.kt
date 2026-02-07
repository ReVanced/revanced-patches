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

private val copyVideoURLResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        playerControlsPatch,
        addResourcesPatch,
    )

    apply {
        addResources("youtube", "interaction.copyvideourl.copyVideoURLResourcePatch")

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
val copyVideoURLPatch = bytecodePatch(
    name = "Copy video URL",
    description = "Adds options to display buttons in the video player to copy video URLs.",
) {
    dependsOn(
        copyVideoURLResourcePatch,
        playerControlsPatch,
        videoInformationPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "20.14.43",
            "20.21.37",
            "20.26.46",
            "20.31.42",
            "20.37.48",
            "20.40.45"
        ),
    )

    apply {
        val extensionPlayerPackage = "Lapp/revanced/extension/youtube/videoplayer"
        val buttonsDescriptors = listOf(
            "$extensionPlayerPackage/CopyVideoURLButton;",
            "$extensionPlayerPackage/CopyVideoURLTimestampButton;",
        )

        buttonsDescriptors.forEach { descriptor ->
            initializeBottomControl(descriptor)
            injectVisibilityCheckCall(descriptor)
        }
    }
}
