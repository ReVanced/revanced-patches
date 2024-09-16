package app.revanced.patches.youtube.layout.startpage

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.youtube.layout.startpage.fingerprints.StartActivityFingerprint
import app.revanced.patches.youtube.layout.startpage.fingerprints.StartActivityLegacyFingerprint
import app.revanced.patches.youtube.layout.startpage.fingerprints.StartActivityParentFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.playservice.YouTubeVersionCheck
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.alsoResolve
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionReversed
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Change start page",
    description = "Adds an option to set which page the app opens in instead of the homepage.",
    dependencies = [
        IntegrationsPatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class,
        YouTubeVersionCheck::class
    ],
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
                "19.34.42",
            ]
        )
    ]
)
@Suppress("unused")
object ChangeStartPagePatch : BytecodePatch(
    setOf(StartActivityLegacyFingerprint, StartActivityParentFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/ChangeStartPagePatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            ListPreference(
                key = "revanced_start_page",
                entriesKey = if (YouTubeVersionCheck.is_19_32_or_greater) "revanced_start_page_entries"
                else "revanced_start_page_legacy_entries",
                entryValuesKey = if (YouTubeVersionCheck.is_19_32_or_greater) "revanced_start_page_entry_values"
                else "revanced_start_page_legacy_entry_values",
                summaryKey = null,
            )
        )

        if (YouTubeVersionCheck.is_19_32_or_greater) {
            StartActivityFingerprint.alsoResolve(context, StartActivityParentFingerprint).mutableMethod.apply {
                val index = indexOfFirstInstructionReversed {
                    getReference<MethodReference>()?.name == "getIntent"
                }
                val register = getInstruction<OneRegisterInstruction>(index + 1).registerA

                addInstruction(
                    index + 2,
                    "invoke-static { v$register }, $INTEGRATIONS_CLASS_DESCRIPTOR->changeStartPage(Landroid/content/Intent;)V"
                )
            }
        } else {
            StartActivityLegacyFingerprint.resultOrThrow().mutableMethod.addInstruction(
                0,
                "invoke-static { p1 }, $INTEGRATIONS_CLASS_DESCRIPTOR->changeStartPageLegacy(Landroid/content/Intent;)V"
            )
        }
    }
}
