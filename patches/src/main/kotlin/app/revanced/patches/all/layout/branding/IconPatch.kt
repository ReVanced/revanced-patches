package app.revanced.patches.all.layout.branding

import app.revanced.patcher.patch.*
import app.revanced.util.getNode
import app.revanced.util.inputStreamFromBundledResource
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

private const val FULL_ICON = 0
private const val ROUND_ICON = 1
private const val BACKGROUND_ICON = 2
private const val FOREGROUND_ICON = 3
private const val MONOCHROME_ICON = 4

val changeIconPatch = resourcePatch(
    name = "Change icon",
    description = "Changes the app icon to a custom icon. By default, the ReVanced icon is used.",
    use = false,
) {
    val revancedIconOptionValue = emptyList<String>() // Empty list == ReVanced icon.

    val pixelDensities = setOf(
        "mdpi",
        "hdpi",
        "xhdpi",
        "xxhdpi",
        "xxxhdpi",
    )

    val iconOptions = pixelDensities.associateWith { pixelDensity ->
        stringsOption(
            key = "${pixelDensity}Icons",
            default = revancedIconOptionValue,
            values = mapOf("ReVanced logo" to revancedIconOptionValue),
            title = "Icons (Pixel density: $pixelDensity)",
            description = buildString {
                appendLine("Provide paths to the following icons for pixel density $pixelDensity (PNG, JPG, WEBP, or vector drawable XML):")
                appendLine("1. Launcher icon (required)")
                appendLine("2. Round icon (optional, Android 7+)")
                appendLine("\nYou can use adaptive icons (Android 8+) by providing the following additional icons:")
                appendLine("\n3. Background icon (optional)")
                appendLine("4. Foreground icon (optional)")
                appendLine("5. Monochrome icon (optional, Android 13+")
                appendLine("\nIcons must be provided in the same order as listed above. Missing optional icons can be skipped by leaving the field empty.")
                appendLine("\nYou can create custom icon sets at https://icon.kitchen.")
            },
            required = true,
        )
    }

    execute {
        val firstPixelDensity = pixelDensities.first()

        fun patchIcon(
            getIcon: (String, Int) -> String?,
            readIcon: (String) -> InputStream,
        ) {
            // Any density, as the user should provide the icons for all densities.

            // region Change the app icon in the AndroidManifest.xml file.

            // If a round icon is provided, set the android:roundIcon attribute.
            document("AndroidManifest.xml").use {
                it.getNode("application").attributes.apply {
                    getNamedItem("android:icon").textContent = "@mipmap/ic_launcher"

                    val roundIcon = getIcon(firstPixelDensity, ROUND_ICON)
                    if (roundIcon?.isNotEmpty() == true) {
                        val roundIconAttribute = getNamedItem("android:roundIcon")
                            ?: setNamedItem(it.createAttribute("android:roundIcon"))
                        roundIconAttribute.textContent = "@mipmap/ic_launcher_round"
                    }
                }
            }

            // endregion

            // region Change the app icon for each pixel density.

            val hasAdaptiveIcon = getIcon(firstPixelDensity, BACKGROUND_ICON)

            if (hasAdaptiveIcon?.isNotEmpty() == true) {
                val monochromeIconXmlString = if (getIcon(firstPixelDensity, MONOCHROME_ICON)?.isNotEmpty() == true) {
                    "<monochrome android:drawable=\"@drawable/ic_launcher_monochrome\"/>"
                } else {
                    ""
                }

                // If an adaptive icon is provided, add the adaptive icon XML file to the res/mipmap-anydpi directory.
                get("res/mipmap-anydpi/ic_launcher.xml").writeText(
                    """
                        <?xml version="1.0" encoding="utf-8"?>
                            <adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
                            <background android:drawable="@mipmap/ic_launcher_background"/>
                            <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
                            $monochromeIconXmlString
                        </adaptive-icon>
                    """.trimIndent(),
                )
            }

            pixelDensities.forEach { pixelDensity ->
                val icon = getIcon(pixelDensity, FULL_ICON)!!
                // Safe call (?.) is used because the user may just provide the full icon and skip the other optional icons.
                val roundIcon = getIcon(pixelDensity, ROUND_ICON)
                val backgroundIcon = getIcon(pixelDensity, BACKGROUND_ICON)
                val foregroundIcon = getIcon(pixelDensity, FOREGROUND_ICON)
                val monochromeIcon = getIcon(pixelDensity, MONOCHROME_ICON)

                infix fun String?.to(target: String) {
                    if (isNullOrEmpty()) {
                        return
                    }

                    Files.copy(
                        readIcon(this),
                        get("res/$target").toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                    )
                }

                // Copy the icons to the mipmap directory.
                icon to "mipmap-$pixelDensity/ic_launcher.png"
                roundIcon to "mipmap-$pixelDensity/ic_launcher_round.png"
                backgroundIcon to "mipmap-$pixelDensity/ic_launcher_background.png"
                foregroundIcon to "mipmap-$pixelDensity/ic_launcher_foreground.png"
                monochromeIcon to "drawable-$pixelDensity/ic_launcher_monochrome.png"
            }

            // endregion
        }

        if (iconOptions[firstPixelDensity]!!.value === revancedIconOptionValue) {
            patchIcon({ pixelDensity, iconIndex ->
                when (iconIndex) {
                    FULL_ICON -> "mipmap-$pixelDensity/revanced-icon"
                    ROUND_ICON -> "mipmap-$pixelDensity/revanced-icon-round"
                    BACKGROUND_ICON -> "mipmap-$pixelDensity/revanced-icon-background"
                    FOREGROUND_ICON -> "mipmap-$pixelDensity/revanced-icon-foreground"
                    MONOCHROME_ICON -> "drawable-$pixelDensity/revanced-icon-monochrome"
                    else -> throw IllegalArgumentException("Invalid icon index: $iconIndex")
                }
            }) { icon ->
                inputStreamFromBundledResource("change-icon", "$icon.png")!!
            }
        } else {
            patchIcon({ pixelDensity, iconIndex ->
                iconOptions[pixelDensity]?.value?.get(iconIndex)
            }) { icon ->
                get(icon).inputStream()
            }
        }
    }
}
