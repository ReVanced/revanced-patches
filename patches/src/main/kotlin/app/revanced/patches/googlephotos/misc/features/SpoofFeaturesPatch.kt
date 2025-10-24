package app.revanced.patches.googlephotos.misc.features

import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.stringsOption
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Suppress("unused")
val spoofFeaturesPatch = bytecodePatch(
    name = "Spoof features",
    description = "Spoofs the device to enable Google Pixel exclusive features, including unlimited storage.",
) {
    compatibleWith("com.google.android.apps.photos")

    dependsOn(spoofBuildInfoPatch)

    val featuresToEnable by stringsOption(
        key = "featuresToEnable",
        default = listOf(
            "com.google.android.apps.photos.NEXUS_PRELOAD",
            "com.google.android.apps.photos.nexus_preload",
        ),
        title = "Features to enable",
        description = "Google Pixel exclusive features to enable. Features up to Pixel XL enable the unlimited storage feature.",
        required = true,
    )

    val featuresToDisable by stringsOption(
        key = "featuresToDisable",
        default = listOf(
            "com.google.android.apps.photos.PIXEL_2017_PRELOAD",
            "com.google.android.apps.photos.PIXEL_2018_PRELOAD",
            "com.google.android.apps.photos.PIXEL_2019_MIDYEAR_PRELOAD",
            "com.google.android.apps.photos.PIXEL_2019_PRELOAD",
            "com.google.android.feature.PIXEL_2020_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2020_EXPERIENCE",
            "com.google.android.feature.PIXEL_2021_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2021_EXPERIENCE",
            "com.google.android.feature.PIXEL_2022_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2022_EXPERIENCE",
            "com.google.android.feature.PIXEL_2023_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2023_EXPERIENCE",
            "com.google.android.feature.PIXEL_2024_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2024_EXPERIENCE",
            "com.google.android.feature.PIXEL_2025_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2025_EXPERIENCE",
        ),
        title = "Features to disable",
        description = "Google Pixel exclusive features to disable." +
            "Features after Pixel XL may have to be disabled for unlimited storage depending on the device.",
        required = true,
    )

    execute {
        @Suppress("NAME_SHADOWING")
        val featuresToEnable = featuresToEnable!!.toSet()

        @Suppress("NAME_SHADOWING")
        val featuresToDisable = featuresToDisable!!.toSet()

        initializeFeaturesEnumFingerprint.method.apply {
            instructions.filter { it.opcode == Opcode.CONST_STRING }.forEach {
                val feature = it.getReference<StringReference>()!!.string

                val spoofedFeature = when (feature) {
                    in featuresToEnable -> "android.hardware.wifi"
                    in featuresToDisable -> "dummy"
                    else -> return@forEach
                }

                val constStringIndex = it.location.index
                val constStringRegister = (it as OneRegisterInstruction).registerA

                replaceInstruction(
                    constStringIndex,
                    "const-string v$constStringRegister, \"$spoofedFeature\"",
                )
            }
        }
    }
}
