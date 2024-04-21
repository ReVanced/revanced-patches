package app.revanced.patches.youtube.misc.dimensions.spoof

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.dimensions.spoof.fingerprints.DeviceDimensionsModelToStringFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.exception

@Patch(
    name = "Spoof device dimensions",
    description = "Adds an option to spoof the device dimensions which can unlock higher video qualities.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class, AddResourcesPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
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
object SpoofDeviceDimensionsPatch : BytecodePatch(
    setOf(DeviceDimensionsModelToStringFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/spoof/SpoofDeviceDimensionsPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_spoof_device_dimensions")
        )

        DeviceDimensionsModelToStringFingerprint.result
            ?.mutableClass?.methods?.find { method -> method.name == "<init>" }
            // Override the parameters containing the dimensions.
            ?.addInstructions(
                1, // Add after super call.
                mapOf(
                    1 to "MinHeightOrWidth", // p1 = min height
                    2 to "MaxHeightOrWidth", // p2 = max height
                    3 to "MinHeightOrWidth", // p3 = min width
                    4 to "MaxHeightOrWidth"  // p4 = max width
                ).map { (parameter, method) ->
                    """
                        invoke-static { p$parameter }, $INTEGRATIONS_CLASS_DESCRIPTOR->get$method(I)I
                        move-result p$parameter
                    """
                }.joinToString("\n") { it }
            ) ?: throw DeviceDimensionsModelToStringFingerprint.exception
    }
}