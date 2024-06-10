package app.revanced.patches.youtube.layout.spoofappversion

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.spoofappversion.fingerprints.spoofAppVersionFingerprint
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val INTEGRATIONS_CLASS_DESCRIPTOR =
    "Lapp/revanced/integrations/youtube/patches/spoof/SpoofAppVersionPatch;"

@Suppress("unused")
val spoofAppVersionPatch = bytecodePatch(
    name = "Spoof app version",
    description = "Adds an option to trick YouTube into thinking you are running an older version of the app. " +
        "This can be used to restore old UI elements and features.",
) {
    dependsOn(
        integrationsPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
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
            "19.11.43",
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    val spoofAppVersionResult by spoofAppVersionFingerprint

    execute {
        addResources("youtube", "layout.spoofappversion.SpoofAppVersionPatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_spoof_app_version"),
            ListPreference(
                key = "revanced_spoof_app_version_target",
                summaryKey = null,
            ),
        )

        val insertIndex = spoofAppVersionResult.scanResult.patternScanResult!!.startIndex + 1
        val buildOverrideNameRegister =
            spoofAppVersionResult.mutableMethod.getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA

        spoofAppVersionResult.mutableMethod.addInstructions(
            insertIndex,
            """
                invoke-static {v$buildOverrideNameRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->getYouTubeVersionOverride(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v$buildOverrideNameRegister
            """,
        )
    }
}
