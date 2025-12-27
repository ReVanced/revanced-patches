package app.revanced.patches.all.misc.optimization

import android.os.Build.SUPPORTED_ABIS
import app.revanced.patcher.patch.rawResourcePatch
import app.revanced.patcher.patch.stringsOption
import app.revanced.util.isAndroid

@Suppress("unused")
val stripPlatformLibrariesPatch = rawResourcePatch(
    "Strip platform libraries",
    "Removes unused platform-native libraries from the APK to reduce package size" +
            "- if detected automatically, the device's unsupported ABIs by default."
) {
    val allAbis = listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
    val supportedAbis = if (isAndroid) SUPPORTED_ABIS.toList() else allAbis

    val platformsToKeep by stringsOption(
        key = "platformsToKeep",
        title = "Platforms to keep",
        description = "The platforms to keep in the APK.",
        default = supportedAbis,
        values = mapOf("Keep all" to allAbis) + allAbis.associate { "Only $it" to listOf(it) },
        required = true
    )
    
    execute {
        val platforms = platformsToKeep!!
        get("libs").listFiles { it.name !in platforms }?.forEach { it.deleteRecursively() }
    }
}
