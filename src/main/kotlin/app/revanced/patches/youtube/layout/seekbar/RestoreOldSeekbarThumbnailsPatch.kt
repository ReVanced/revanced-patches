package app.revanced.patches.youtube.layout.seekbar

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.seekbar.fingerprints.FullscreenSeekbarThumbnailsFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.playservice.VersionCheckPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.exception

@Patch(
    name = "Restore old seekbar thumbnails",
    description = "Adds an option to restore the old seekbar thumbnails that appear above the seekbar while seeking instead of fullscreen thumbnails.",
    dependencies = [IntegrationsPatch::class, AddResourcesPatch::class, VersionCheckPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.38.44",
                "18.49.37",
                "19.16.39"
                // 19.17+ is not supported.
            ]
        )
    ]
)
@Suppress("unused")
object RestoreOldSeekbarThumbnailsPatch : BytecodePatch(
    setOf(FullscreenSeekbarThumbnailsFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/RestoreOldSeekbarThumbnailsPatch;"

    override fun execute(context: BytecodeContext) {
        if (VersionCheckPatch.is_19_17_or_greater) {
            // Give a more informative error, if the user has turned off version checks.
            throw PatchException("'Restore old seekbar thumbnails' cannot be patched to any version after 19.16.39")
        }

        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.SEEKBAR.addPreferences(
            SwitchPreference("revanced_restore_old_seekbar_thumbnails")
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
