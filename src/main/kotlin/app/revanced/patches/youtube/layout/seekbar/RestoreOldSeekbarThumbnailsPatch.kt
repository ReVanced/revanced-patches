package app.revanced.patches.youtube.layout.seekbar

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.seekbar.fingerprints.FullscreenSeekbarThumbnailsFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Restore old seekbar thumbnails",
    description = "Restores the old seekbar thumbnails that appear above the seekbar instead of fullscreen thumbnails.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.41"
            ]
        )
    ]
)
@Suppress("unused")
object RestoreOldSeekbarThumbnailsPatch : BytecodePatch(
    setOf(FullscreenSeekbarThumbnailsFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/patches/RestoreOldSeekbarThumbnailsPatch;"

    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_restore_old_seekbar_thumbnails",
                StringResource(
                    "revanced_restore_old_seekbar_thumbnails_title",
                    "Restore old seekbar thumbnails"
                ),
                StringResource(
                    "revanced_restore_old_seekbar_thumbnails_summary_on",
                    "Seekbar thumbnails will appear above the seekbar"
                ),
                StringResource(
                    "revanced_restore_old_seekbar_thumbnails_summary_off",
                    "Seekbar thumbnails will appear in fullscreen"
                ),
            )
        )

        FullscreenSeekbarThumbnailsFingerprint.result?.mutableMethod?.apply {
            val moveResultIndex = getInstructions().lastIndex - 1

            addInstruction(
                moveResultIndex,
                "invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->useFullscreenSeekbarThumbnails()Z"
            )
        } ?: throw FullscreenSeekbarThumbnailsFingerprint.exception
    }
}
