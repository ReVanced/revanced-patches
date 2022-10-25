package app.revanced.patches.youtube.layout.hidealbumcards.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.layout.hidealbumcards.annotations.AlbumCardsCompatibility
import app.revanced.patches.youtube.layout.hidealbumcards.fingerprints.AlbumCardsFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.mapping.patch.ResourceMappingResourcePatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class, ResourceMappingResourcePatch::class])
@Name("hide-album-cards")
@Description("Hides the album cards below the artist description.")
@AlbumCardsCompatibility
@Version("0.0.1")
class AlbumCardsPatch : BytecodePatch(
    listOf(
        AlbumCardsFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_album_cards",
                StringResource("revanced_hide_album_cards_title", "Hide the album cards"),
                false,
                StringResource("revanced_hide_album_cards_summary_on", "Album cards is hidden"),
                StringResource("revanced_hide_album_cards_summary_off", "Album cards is visible")
            )
        )

        val albumCardsResult = AlbumCardsFingerprint.result!!
        val albumCardsMethod = albumCardsResult.mutableMethod

        val checkCastIndex = albumCardsResult.scanResult.patternScanResult!!.endIndex
        val patchIndex = checkCastIndex + 1

        albumCardsMethod.addInstruction(
            patchIndex, """
            invoke-static {v${(albumCardsMethod.instruction(checkCastIndex) as OneRegisterInstruction).registerA}}, Lapp/revanced/integrations/patches/HideAlbumCardsPatch;->hideAlbumCards(Landroid/view/View;)V
        """
        )

        return PatchResultSuccess()
    }
}
