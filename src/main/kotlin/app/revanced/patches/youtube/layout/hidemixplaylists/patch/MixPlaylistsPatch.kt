package app.revanced.patches.youtube.layout.hidemixplaylists.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.layout.hidemixplaylists.annotations.MixPlaylistsPatchCompatibility
import app.revanced.patches.youtube.layout.hidemixplaylists.fingerprints.MixPlaylistsPatchFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@DependsOn([IntegrationsPatch::class])
@Name("hide-my-mix")
@Description("Removes mix playlists from the feed.")
@MixPlaylistsPatchCompatibility
@Version("0.0.1")
class MixPlaylistsPatch : BytecodePatch(
    listOf(
        MixPlaylistsPatchFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_mix_playlists_hidden",
                StringResource("revanced_mix_playlists_title", "Hide mix playlists"),
                false,
                StringResource("revanced_mix_playlists_summary_on", "Mix playlists are hidden"),
                StringResource("revanced_mix_playlists_summary_off", "Mix playlists are shown")
            )
        )

        val result = MixPlaylistsPatchFingerprint.result!!
        val method = result.mutableMethod
        val index = result.scanResult.patternScanResult!!.endIndex - 6
        val register = (method.implementation!!.instructions[index] as OneRegisterInstruction).registerA

        method.addInstruction(
            index + 2,
            "invoke-static {v$register}, Lapp/revanced/integrations/patches/HideMixPlaylistsPatch;->hideMixPlaylists(Landroid/view/View;)V"
        )

        return PatchResultSuccess()
    }
}
