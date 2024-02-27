package app.revanced.patches.youtube.layout.branding.icon

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.patches.youtube.utils.settings.ResourceUtils.updatePatchStatusIcon
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import org.w3c.dom.Element
import java.io.File
import java.nio.file.Files

@Patch(
    name = "Custom branding icon YouTube",
    description = "Change the YouTube launcher icon to the icon specified in options.json.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object CustomBrandingIconPatch : ResourcePatch() {
    private const val DEFAULT_ICON_KEY = "Revancify Blue"

    private val availableIcon = mapOf(
        "MMT" to "mmt",
        DEFAULT_ICON_KEY to "revancify_blue",
        "Revancify Red" to "revancify_red"
    )

    private val drawableIconResourceFileNames = arrayOf(
        "product_logo_youtube_color_24",
        "product_logo_youtube_color_36",
        "product_logo_youtube_color_144",
        "product_logo_youtube_color_192"
    ).map { "$it.png" }.toTypedArray()

    private val drawableDirectories = arrayOf(
        "xxxhdpi",
        "xxhdpi",
        "xhdpi",
        "hdpi",
        "mdpi"
    ).map { "drawable-$it" }

    private val mipmapIconResourceFileNames = arrayOf(
        "adaptiveproduct_youtube_background_color_108",
        "adaptiveproduct_youtube_foreground_color_108",
        "ic_launcher",
        "ic_launcher_round"
    ).map { "$it.png" }.toTypedArray()

    private val mipmapDirectories = arrayOf(
        "xxxhdpi",
        "xxhdpi",
        "xhdpi",
        "hdpi",
        "mdpi"
    ).map { "mipmap-$it" }

    private var AppIcon by stringPatchOption(
        key = "AppIcon",
        default = DEFAULT_ICON_KEY,
        values = availableIcon,
        title = "App icon",
        description = """
            The path to a folder must contain one or more of the following folders matching the DPI of your device:

            ${mipmapDirectories.joinToString("\n") { "- $it" }}

            Each of these folders has to have the following files:

            ${mipmapIconResourceFileNames.joinToString("\n") { "- $it" }}
            """
            .split("\n")
            .joinToString("\n") { it.trimIndent() } // Remove the leading whitespace from each line.
            .trimIndent(), // Remove the leading newline.
    )

    override fun execute(context: ResourceContext) {
        AppIcon?.let { appIcon ->
            val appIconValue = appIcon.lowercase().replace(" ","_")
            if (!availableIcon.containsValue(appIconValue)) {
                mipmapDirectories.map { directory ->
                    ResourceGroup(
                        directory, *mipmapIconResourceFileNames
                    )
                }.let { resourceGroups ->
                    try {
                        val path = File(appIcon)
                        val resourceDirectory = context["res"]

                        resourceGroups.forEach { group ->
                            val fromDirectory = path.resolve(group.resourceDirectoryName)
                            val toDirectory = resourceDirectory.resolve(group.resourceDirectoryName)

                            group.resources.forEach { iconFileName ->
                                Files.write(
                                    toDirectory.resolve(iconFileName).toPath(),
                                    fromDirectory.resolve(iconFileName).readBytes()
                                )
                            }
                        }
                        context.updatePatchStatusIcon("custom")
                    } catch (_: Exception) {
                        throw PatchException("Invalid app icon path: $appIcon")
                    }
                }
            } else {
                val resourcePath = "youtube/branding/$appIconValue"

                // change launcher icon.
                mipmapDirectories.map { directory ->
                    ResourceGroup(
                        directory, *mipmapIconResourceFileNames
                    )
                }.let { resourceGroups ->
                    resourceGroups.forEach {
                        context.copyResources("$resourcePath/launcher", it)
                    }
                }

                // change splash icon.
                drawableDirectories.map { directory ->
                    ResourceGroup(
                        directory, *drawableIconResourceFileNames
                    )
                }.let { resourceGroups ->
                    resourceGroups.forEach {
                        context.copyResources("$resourcePath/splash", it)
                    }
                }

                // change monochrome icon.
                arrayOf(
                    ResourceGroup(
                        "drawable",
                        "adaptive_monochrome_ic_youtube_launcher.xml"
                    )
                ).forEach { resourceGroup ->
                    context.copyResources("$resourcePath/monochrome", resourceGroup)
                }

                // disable splash animation.
                context.xmlEditor["res/values-v31/styles.xml"].use { editor ->
                    val tags = editor.file.getElementsByTagName("item")
                    List(tags.length) { tags.item(it) as Element }
                        .filter { it.getAttribute("name").contains("android:windowSplashScreenAnimatedIcon") }
                        .forEach { it.parentNode.removeChild(it) }
                }

                context.updatePatchStatusIcon(appIconValue)
            }
        } ?: throw PatchException("Invalid app icon path.")
    }
}