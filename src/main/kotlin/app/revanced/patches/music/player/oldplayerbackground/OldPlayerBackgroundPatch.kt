package app.revanced.patches.music.player.oldplayerbackground

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.player.oldplayerbackground.fingerprints.OldPlayerBackgroundFingerprint
import app.revanced.patches.music.utils.integrations.Constants.PLAYER
import app.revanced.patches.music.utils.settings.CategoryType
import app.revanced.patches.music.utils.settings.SettingsPatch

@Patch(
    name = "Enable old player background",
    description = "Adds an option to return the player background to the old style. Deprecated on YT Music 6.34.51+.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.apps.youtube.music",
            [
                "6.21.52",
                "6.22.52",
                "6.23.56",
                "6.25.53",
                "6.26.51",
                "6.27.54",
                "6.28.53",
                "6.29.58",
                "6.31.55",
                "6.33.52"
            ]
        )
    ],
    use = false
)
@Suppress("unused")
object OldPlayerBackgroundPatch : BytecodePatch(
    setOf(OldPlayerBackgroundFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        OldPlayerBackgroundFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstructions(
                    0, """
                        invoke-static {}, $PLAYER->enableOldPlayerBackground()Z
                        move-result v0
                        return v0
                        """
                )
            }
        } ?: throw PatchException("This version is not supported. Please use YT Music 6.33.52 or earlier.")

        SettingsPatch.addMusicPreference(
            CategoryType.PLAYER,
            "revanced_enable_old_player_background",
            "false"
        )

    }
}