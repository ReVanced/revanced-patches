package app.revanced.patches.music.player.minimizedplayer

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.player.minimizedplayer.fingerprints.MinimizedPlayerFingerprint
import app.revanced.patches.music.utils.integrations.Constants.PLAYER
import app.revanced.patches.music.utils.settings.CategoryType
import app.revanced.patches.music.utils.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Enable force minimized player",
    description = "Adds an option to keep the miniplayer minimized even when another track is played.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object MinimizedPlayerPatch : BytecodePatch(
    setOf(MinimizedPlayerFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        MinimizedPlayerFingerprint.result?.let {
            with(it.mutableMethod) {
                val index = it.scanResult.patternScanResult!!.endIndex
                val register =
                    (implementation!!.instructions[index] as OneRegisterInstruction).registerA

                addInstructions(
                    index, """
                        invoke-static {v$register}, $PLAYER->enableForceMinimizedPlayer(Z)Z
                        move-result v$register
                        """
                )
            }
        } ?: throw MinimizedPlayerFingerprint.exception

        SettingsPatch.addMusicPreference(
            CategoryType.PLAYER,
            "revanced_enable_force_minimized_player",
            "true"
        )

    }
}