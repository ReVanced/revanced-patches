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
    description = "Changes the YouTube launcher icon and name to your choice (defaults to ReVanced).",
    compatiblePackages = [
        CompatiblePackage("com.google.android.youtube")
    ],
    use = false
)
@Suppress("unused")
object CustomBrandingPatch : ResourcePatch() {
    private var appName by stringPatchOption(
        key = "appName",
        default = "YouTube ReVanced",
        title = "Application Name",
        description = "The name of the application it will show on your home screen.",
        required = true
    )

    private var iconPath by stringPatchOption(
        key = "iconPath",
        default = null,
        title = "App Icon Path",
        description = "A path containing mipmap resource folders with icons."
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

        val iconResourceFileNames = arrayOf(
            "adaptiveproduct_youtube_background_color_108",
            "adaptiveproduct_youtube_foreground_color_108",
            "ic_launcher",
            "ic_launcher_round"
        ).map { "$it.png" }.toTypedArray()

        fun createGroup(directory: String) = ResourceUtils.ResourceGroup(
            directory, *iconResourceFileNames
        )

        // change the app icon
        arrayOf("xxxhdpi", "xxhdpi", "xhdpi", "hdpi", "mdpi")
            .map { "mipmap-$it" }
            .map(::createGroup)
            .let(::copyResources)

        // change the name of the app
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
