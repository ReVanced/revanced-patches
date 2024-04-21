package app.revanced.patches.youtube.layout.spoofappversion

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.spoofappversion.fingerprints.SpoofAppVersionFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Spoof app version",
    description = "Adds an option to trick YouTube into thinking you are running an older version of the app. " +
            "This can be used to restore old UI elements and features.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class, AddResourcesPatch::class],
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
object SpoofAppVersionPatch : BytecodePatch(
    setOf(SpoofAppVersionFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/spoof/SpoofAppVersionPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_spoof_app_version"),
            ListPreference(
                key = "revanced_spoof_app_version_target",
                summaryKey = null,
            )
        )

        SpoofAppVersionFingerprint.result?.apply {
            val insertIndex = scanResult.patternScanResult!!.startIndex + 1
            val buildOverrideNameRegister =
                (mutableMethod.implementation!!.instructions[insertIndex - 1] as OneRegisterInstruction).registerA

            mutableMethod.addInstructions(
                insertIndex,
                """
                    invoke-static {v$buildOverrideNameRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->getYouTubeVersionOverride(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$buildOverrideNameRegister
                """
            )
        } ?: throw SpoofAppVersionFingerprint.exception
    }
}
