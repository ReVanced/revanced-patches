package app.revanced.patches.youtube.interaction.copyvideourl

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.misc.playercontrols.PlayerControlsBytecodePatch
import app.revanced.patches.youtube.video.information.VideoInformationPatch

@Patch(
    name = "Copy video URL",
    description = "Adds options to display buttons in the video player to copy video URLs.",
    dependencies = [
        CopyVideoUrlResourcePatch::class,
        PlayerControlsBytecodePatch::class,
        VideoInformationPatch::class,
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.38.44",
                "18.49.37",
                "19.16.39",
                "19.25.37",
                "19.34.42",
            ],
        ),
    ],
)
@Suppress("unused")
object CopyVideoUrlBytecodePatch : BytecodePatch(emptySet()) {
    private const val INTEGRATIONS_PLAYER_PACKAGE = "Lapp/revanced/integrations/youtube/videoplayer"
    private val BUTTONS_DESCRIPTORS = listOf(
        "$INTEGRATIONS_PLAYER_PACKAGE/CopyVideoUrlButton;",
        "$INTEGRATIONS_PLAYER_PACKAGE/CopyVideoUrlTimestampButton;",
    )

    override fun execute(context: BytecodeContext) {
        BUTTONS_DESCRIPTORS.forEach { descriptor ->
            PlayerControlsBytecodePatch.initializeBottomControl(descriptor)
            PlayerControlsBytecodePatch.injectVisibilityCheckCall(descriptor)
        }
    }
}
