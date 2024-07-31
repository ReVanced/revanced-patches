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
        bottomControlsPatch,
        addResourcesPatch,
    )

    execute { context ->
        addResources("youtube", "interaction.copyvideourl.copyVideoUrlResourcePatch")

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

        addBottomControls("copyvideourl")
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
            "18.48.39",
            "18.49.37",
            "19.01.34",
            "19.02.39",
            "19.03.36",
            "19.04.38",
            "19.05.36",
            "19.06.39",
            "19.07.40",
            "19.08.36",
            "19.09.38",
            "19.10.39",
            "19.11.43",
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    execute {
        val extensionPlayerPackage = "Lapp/revanced/extension/youtube/videoplayer"
        val buttonsDescriptors = listOf(
            "$extensionPlayerPackage/CopyVideoUrlButton;",
            "$extensionPlayerPackage/CopyVideoUrlTimestampButton;",
        )

        buttonsDescriptors.forEach { descriptor ->
            initializeControl("$descriptor->initializeButton(Landroid/view/View;)V")
            injectVisibilityCheckCall("$descriptor->changeVisibility(Z)V")
        }
    }
}
