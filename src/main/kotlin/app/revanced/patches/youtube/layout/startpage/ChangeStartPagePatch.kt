package app.revanced.patches.youtube.layout.startpage

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.impl.ListPreference
import app.revanced.patches.youtube.layout.startpage.fingerprints.StartActivityFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.shared.fingerprints.HomeActivityFingerprint
import app.revanced.util.exception
import app.revanced.util.resource.ArrayResource

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

        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            ListPreference(
                key = "revanced_start_page",
                titleKey = "revanced_start_page_title",
                summaryKey = null,
                entries = ArrayResource(
                    "revanced_start_page_entries",
                    listOf(
                        "revanced_start_page_home_entry_0",
                        "revanced_start_page_home_entry_1",
                        "revanced_start_page_home_entry_2",
                        "revanced_start_page_home_entry_3",
                        "revanced_start_page_home_entry_4",
                        "revanced_start_page_home_entry_5",
                    )
                ),
                entryValues = ArrayResource(
                    "revanced_start_page_values",
                    listOf(
                        "",
                        "MAIN",
                        "open.search",
                        "open.subscriptions",
                        "open.explore",
                        "open.shorts",
                    ),
                    literalValues = true
                )
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
