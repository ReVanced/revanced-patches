package app.revanced.patches.youtube.interaction.copyvideourl

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.misc.playercontrols.PlayerControlsBytecodePatch
import app.revanced.patches.youtube.video.information.VideoInformationPatch

@Patch(
    name = "Copy video url",
    description = "Adds buttons in player to copy video links.",
    dependencies = [
        CopyVideoUrlResourcePatch::class,
        PlayerControlsBytecodePatch::class,
        VideoInformationPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.16.37",
                "18.19.35",
                "18.20.39",
                "18.23.35",
                "18.29.38",
                "18.32.39"
            ]
        )
    ]
)
@Suppress("unused")
object CopyVideoUrlBytecodePatch : BytecodePatch() {
    private const val INTEGRATIONS_PLAYER_PACKAGE = "Lapp/revanced/integrations/videoplayer"
    private val BUTTONS_DESCRIPTORS = listOf(
        "$INTEGRATIONS_PLAYER_PACKAGE/CopyVideoUrlButton;",
        "$INTEGRATIONS_PLAYER_PACKAGE/CopyVideoUrlTimestampButton;"
    )

    override fun execute(context: BytecodeContext) {
        // Initialize buttons and inject visibility control
        BUTTONS_DESCRIPTORS.forEach { descriptor ->
            PlayerControlsBytecodePatch.initializeControl("$descriptor->initializeButton(Landroid/view/View;)V")
            PlayerControlsBytecodePatch.injectVisibilityCheckCall("$descriptor->changeVisibility(Z)V")
        }
    }
}