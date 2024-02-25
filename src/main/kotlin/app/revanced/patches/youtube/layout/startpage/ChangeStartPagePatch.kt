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
import app.revanced.patches.youtube.shared.fingerprints.HomeActivityFingerprint
import app.revanced.util.exception

@Patch(
    name = "Change start page",
    description = "Adds an option to set which page the app opens in instead of the homepage.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class, AddResourcesPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube"
        )
    ]
)
@Suppress("unused")
object ChangeStartPagePatch : BytecodePatch(
    setOf(HomeActivityFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/ChangeStartPagePatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            ListPreference(
                key = "revanced_start_page",
                summaryKey = null,
            )
        )

        StartActivityFingerprint.resolve(
            context,
            HomeActivityFingerprint.result?.classDef ?: throw HomeActivityFingerprint.exception
        )

        StartActivityFingerprint.result?.mutableMethod?.addInstruction(
            0,
            "invoke-static { p1 }, $INTEGRATIONS_CLASS_DESCRIPTOR->changeIntent(Landroid/content/Intent;)V"
        ) ?: throw StartActivityFingerprint.exception
    }
}
