package app.revanced.patches.youtube.layout.branding

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.types.StringPatchOption.Companion.stringPatchOption
import app.revanced.util.resources.ResourceUtils
import app.revanced.util.resources.ResourceUtils.copyResources
import java.io.File
import java.nio.file.Files

@Patch(
    name = "Custom branding",
    description = "Changes the app icon and name to your choice (defaults to YouTube ReVanced and the ReVanced logo).",
    compatiblePackages = [
        CompatiblePackage("com.google.android.youtube")
    ],
    use = false
)
@Suppress("unused")
object CustomBrandingPatch : ResourcePatch() {
    private val iconResourceFileNames = arrayOf(
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

    private var appName by stringPatchOption(
        key = "appName",
        default = "YouTube ReVanced",
        title = "App name",
        description = "The name of the app.",
        required = true
    )

    private var iconPath by stringPatchOption(
        key = "iconPath",
        default = null,
        title = "App icon path",
        description = """
            The path to a folder containing the following folders:

            ${mipmapDirectories.joinToString("\n") { "- $it" }}

            Each of these folders has to have the following files:

            ${iconResourceFileNames.joinToString("\n") { "- $it" }}
        """
            .split("\n")
            .joinToString("\n") { it.trimIndent() } // Remove the leading whitespace from each line.
            .trimIndent(), // Remove the leading newline.
    )

    override fun execute(context: ResourceContext) {
        fun copyResources(resourceGroups: List<ResourceUtils.ResourceGroup>) {
            iconPath?.let { iconPathString ->
                val iconPath = File(iconPathString)
                val resourceDirectory = context["res"]

                resourceGroups.forEach { group ->
                    val fromDirectory = iconPath.resolve(group.resourceDirectoryName)
                    val toDirectory = resourceDirectory.resolve(group.resourceDirectoryName)

                    group.resources.forEach { iconFileName ->
                        Files.write(
                            toDirectory.resolve(iconFileName).toPath(),
                            fromDirectory.resolve(iconFileName).readBytes()
                        )
                    }
                }
            } ?: resourceGroups.forEach { context.copyResources("branding", it) }
        }

        fun createGroup(directory: String) = ResourceUtils.ResourceGroup(
            directory, *iconResourceFileNames
        )

        // Change the app icon.
        mipmapDirectories.map(::createGroup).let(::copyResources)

        // Change the app name.
        val manifest = context["AndroidManifest.xml"]
        manifest.writeText(
            manifest.readText()
                .replace(
                    "android:label=\"@string/application_name",
                    "android:label=\"$appName"
                )
        )
    }
}
