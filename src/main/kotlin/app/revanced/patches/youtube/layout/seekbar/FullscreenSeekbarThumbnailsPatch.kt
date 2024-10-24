package app.revanced.patches.youtube.layout.seekbar

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.seekbar.fingerprints.FullscreenSeekbarThumbnailsQualityFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.resultOrThrow

@Patch(
    name = "Fullscreen seekbar thumbnails",
    description = "Adds an option to use high quality fullscreen seekbar thumbnails.",
    dependencies = [IntegrationsPatch::class, AddResourcesPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.38.44",
                "18.49.37",
                "19.16.39",
                "19.25.37",
                "19.34.42",
            ]
        )
    ]
)
@Suppress("unused")
object FullscreenSeekbarThumbnailsPatch : BytecodePatch(
    setOf(FullscreenSeekbarThumbnailsQualityFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/FullscreenSeekbarThumbnailsPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.SEEKBAR.addPreferences(
            SwitchPreference("revanced_seekbar_fullscreen_high_quality")
        )

        FullscreenSeekbarThumbnailsQualityFingerprint.resultOrThrow().mutableMethod.addInstructions(
            0,
            """
                invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->useHighQualityFullscreenThumbnails()Z
                move-result v0
                return v0
            """
        )
    }
}
