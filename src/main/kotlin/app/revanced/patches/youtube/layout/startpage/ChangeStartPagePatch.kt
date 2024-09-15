package app.revanced.patches.youtube.layout.startpage

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.youtube.layout.startpage.fingerprints.StartActivityFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.resultOrThrow

@Patch(
    name = "Change start page",
    description = "Adds an option to set which page the app opens in instead of the homepage.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class, AddResourcesPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
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
                "19.11.43",
                "19.12.41",
                "19.13.37",
                "19.14.43",
                "19.15.36",
                "19.16.39",
                "19.25.37",
                // 19.31.36 // Last version that works with this patch in it's current form.
                // 19.32+   // Needs changes for this patch to work.
            ]
        )
    ]
)
@Suppress("unused")
object ChangeStartPagePatch : BytecodePatch(
    setOf(StartActivityFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/ChangeStartPagePatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        StartActivityFingerprint.resultOrThrow().mutableMethod.addInstruction(
            0,
            "invoke-static { p1 }, $INTEGRATIONS_CLASS_DESCRIPTOR->changeIntent(Landroid/content/Intent;)V"
        )

        // Add settings only after resolving, in case the user turns off version checks and is patching 19.32+
        SettingsPatch.PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            ListPreference(
                key = "revanced_start_page",
                summaryKey = null,
            )
        )
    }
}
