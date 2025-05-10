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
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.47.53",
            "20.07.39",
            "20.12.46",
        )
    )

    execute {
        addResources("youtube", "misc.dimensions.spoof.spoofDeviceDimensionsPatch")

        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_spoof_device_dimensions"),
        )

        deviceDimensionsModelToStringFingerprint
            .classDef.methods.first { method -> method.name == "<init>" }
            // Override the parameters containing the dimensions.
            .addInstructions(
                1, // Add after super call.
                arrayOf(
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
