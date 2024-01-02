package app.revanced.patches.youtube.layout.startpage

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.fingerprints.HomeActivityFingerprint
import app.revanced.patches.shared.settings.preference.impl.ArrayResource
import app.revanced.patches.shared.settings.preference.impl.ListPreference
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.youtube.layout.startpage.fingerprints.StartActivityFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.exception

@Patch(
    name = "Change start page",
    description = "Adds an option to set which page the app opens in instead of the homepage.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
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
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            ListPreference(
                "revanced_start_page",
                StringResource(
                    "revanced_start_page_title",
                    "Set start page"
                ),
                ArrayResource(
                    "revanced_start_page_entries",
                    listOf(
                        StringResource("revanced_start_page_home_entry_0", "Default"),
                        StringResource("revanced_start_page_home_entry_1", "Home"),
                        StringResource("revanced_start_page_search_entry_2", "Search"),
                        StringResource("revanced_start_page_subscriptions_entry_3", "Subscriptions"),
                        StringResource("revanced_start_page_explore_entry_4", "Explore"),
                        StringResource("revanced_start_page_shorts_entry_5", "Shorts"),
                    )
                ),
                ArrayResource(
                    "revanced_start_page_values",
                    listOf(
                        StringResource("revanced_start_page_home_value_0", ""),
                        StringResource("revanced_start_page_home_value_1", "MAIN"),
                        StringResource("revanced_start_page_search_value_2", "open.search"),
                        StringResource("revanced_start_page_subscriptions_value_3", "open.subscriptions"),
                        StringResource("revanced_start_page_explore_value_4", "open.explore"),
                        StringResource("revanced_start_page_shorts_value_5", "open.shorts"),
                    )
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
