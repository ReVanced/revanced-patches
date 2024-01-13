package app.revanced.patches.youtube.layout.hide.seekbar

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.seekbar.SeekbarColorBytecodePatch
import app.revanced.patches.youtube.layout.seekbar.SeekbarPreferencesPatch
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.shared.fingerprints.SeekbarFingerprint
import app.revanced.patches.youtube.shared.fingerprints.SeekbarOnDrawFingerprint

@Patch(
    name = "Hide seekbar",
    description = "Adds an option to hide the seekbar.",
    dependencies = [
        IntegrationsPatch::class,
        SettingsPatch::class,
        SeekbarColorBytecodePatch::class,
        SeekbarPreferencesPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34"
            ]
        )
    ]
)
@Suppress("unused")
object HideSeekbarPatch : BytecodePatch(
    setOf(SeekbarFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        SeekbarPreferencesPatch.addPreferences(
            SwitchPreference(
                "revanced_hide_seekbar",
                StringResource("revanced_hide_seekbar_title", "Hide seekbar in video player"),
                StringResource("revanced_hide_seekbar_summary_on", "Video player seekbar is hidden"),
                StringResource("revanced_hide_seekbar_summary_off", "Video player seekbar is shown")
            ),
            SwitchPreference(
                "revanced_hide_seekbar_thumbnail",
                StringResource("revanced_hide_seekbar_thumbnail_title", "Hide seekbar in video thumbnails"),
                StringResource("revanced_hide_seekbar_thumbnail_summary_on", "Thumbnail seekbar is hidden"),
                StringResource("revanced_hide_seekbar_thumbnail_summary_off", "Thumbnail seekbar is shown")
            )
        )

        SeekbarFingerprint.result!!.let {
            SeekbarOnDrawFingerprint.apply { resolve(context, it.mutableClass) }
        }.result!!.mutableMethod.addInstructionsWithLabels(
            0,
            """
                const/4 v0, 0x0
                invoke-static { }, Lapp/revanced/integrations/youtube/patches/HideSeekbarPatch;->hideSeekbar()Z
                move-result v0
                if-eqz v0, :hide_seekbar
                return-void
                :hide_seekbar
                nop
            """
        )
    }
}
