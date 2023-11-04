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
import app.revanced.patches.youtube.layout.seekbar.fingerprints.EnableNewSeekbarThumbnailsFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Enable old seekbar thumbnails",
    description = "Enables the old seekbar thumbnails that appear above the seekbar instead of in fullscreen.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.37.36",
                "18.38.44"
            ]
        )
    ]
)
@Suppress("unused")
object EnableOldSeekbarThumbnailsPatch : BytecodePatch(
    setOf(EnableNewSeekbarThumbnailsFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/patches/EnableOldSeekbarThumbnailsPatch;"

    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_enable_old_seekbar_thumbnails",
                StringResource(
                    "revanced_enable_old_seekbar_thumbnails_title",
                    "Enable old seekbar thumbnails"
                ),
                StringResource(
                    "revanced_enable_old_seekbar_thumbnails_summary_on",
                    "Seekbar thumbnails will appear above the seekbar"
                ),
                StringResource(
                    "revanced_enable_old_seekbar_thumbnails_summary_off",
                    "Seekbar thumbnails will appear in fullscreen"
                ),
            )
        )

        EnableNewSeekbarThumbnailsFingerprint.result?.mutableMethod?.apply {
            val moveResultIndex = getInstructions().lastIndex - 1

            addInstruction(
                moveResultIndex,
                "invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->enableOldSeekbarThumbnails()Z"
            )
        } ?: throw EnableNewSeekbarThumbnailsFingerprint.exception
    }
}
