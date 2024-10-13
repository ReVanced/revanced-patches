package app.revanced.patches.youtube.misc.dimensions.spoof

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/spoof/SpoofDeviceDimensionsPatch;"

@Suppress("unused")
val spoofDeviceDimensionsPatch = bytecodePatch(
    name = "Spoof device dimensions",
    description = "Adds an option to spoof the device dimensions which can unlock higher video qualities.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
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

    val deviceDimensionsModelToStringMatch by deviceDimensionsModelToStringFingerprint()

    execute {
        addResources("youtube", "misc.dimensions.spoof.spoofDeviceDimensionsPatch")

        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_spoof_device_dimensions"),
        )

        deviceDimensionsModelToStringMatch
            .mutableClass.methods.first { method -> method.name == "<init>" }
            // Override the parameters containing the dimensions.
            .addInstructions(
                1, // Add after super call.
                mapOf(
                    1 to "MinHeightOrWidth", // p1 = min height
                    2 to "MaxHeightOrWidth", // p2 = max height
                    3 to "MinHeightOrWidth", // p3 = min width
                    4 to "MaxHeightOrWidth", // p4 = max width
                ).map { (parameter, method) ->
                    """
                        invoke-static { p$parameter }, $EXTENSION_CLASS_DESCRIPTOR->get$method(I)I
                        move-result p$parameter
                    """
                }.joinToString("\n") { it },
            )
    }
}
