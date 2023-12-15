package app.revanced.patches.youtube.layout.seekbar

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.seekbar.fingerprints.FullscreenSeekbarThumbnailsFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.strings.StringsPatch

@Patch(
    name = "Restore old seekbar thumbnails",
    description = "Restores the old seekbar thumbnails that appear above the seekbar instead of fullscreen thumbnails.",
    dependencies = [IntegrationsPatch::class, SeekbarPreferencesPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.41",
                "18.45.43"
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
        StringsPatch.includePatchStrings("RestoreOldSeekbarThumbnails")
        SeekbarPreferencesPatch.addPreferences(
            SwitchPreference(
                "revanced_restore_old_seekbar_thumbnails",
                "revanced_restore_old_seekbar_thumbnails_title",
                "revanced_restore_old_seekbar_thumbnails_summary_on",
                "revanced_restore_old_seekbar_thumbnails_summary_off"
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
