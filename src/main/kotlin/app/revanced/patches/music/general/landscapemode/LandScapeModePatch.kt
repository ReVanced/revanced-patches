package app.revanced.patches.music.general.landscapemode

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.general.landscapemode.fingerprints.TabletIdentifierFingerprint
import app.revanced.patches.music.utils.integrations.Constants.GENERAL
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.music.utils.settings.CategoryType
import app.revanced.patches.music.utils.settings.SettingsPatch
import app.revanced.util.exception

@Patch(
    name = "Enable landscape mode",
    description = "Adds an option to enable landscape mode when rotating the screen on phones.",
    dependencies = [
        SettingsPatch::class,
        SharedResourceIdPatch::class
    ],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object LandScapeModePatch : BytecodePatch(
    setOf(TabletIdentifierFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        TabletIdentifierFingerprint.result?.let {
            it.mutableMethod.addInstructions(
                it.scanResult.patternScanResult!!.endIndex + 1, """
                    invoke-static {p0}, $GENERAL->enableLandScapeMode(Z)Z
                    move-result p0
                    """
            )
        } ?: throw TabletIdentifierFingerprint.exception

        SettingsPatch.addMusicPreference(
            CategoryType.GENERAL,
            "revanced_enable_landscape_mode",
            "true"
        )

    }
}
