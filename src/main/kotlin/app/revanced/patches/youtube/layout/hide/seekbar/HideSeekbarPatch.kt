package app.revanced.patches.youtube.layout.hide.seekbar

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.seekbar.SeekbarColorBytecodePatch
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
        AddResourcesPatch::class
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
                "19.01.34",
                "19.02.39",
                "19.03.36",
                "19.04.38",
                "19.05.36",
                "19.06.39",
                "19.07.40",
                "19.08.36",
                "19.09.38",
                "19.10.39",
                "19.11.43"
            ]
        )
    ]
)
@Suppress("unused")
object HideSeekbarPatch : BytecodePatch(
    setOf(SeekbarFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.SEEKBAR.addPreferences(
            SwitchPreference("revanced_hide_seekbar"),
            SwitchPreference("revanced_hide_seekbar_thumbnail")
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
