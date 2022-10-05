package app.revanced.patches.youtube.layout.branding.icon.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.*
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.layout.branding.icon.annotations.CustomBrandingCompatibility
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Files

@Patch
@DependsOn([FixLocaleConfigErrorPatch::class])
@Name("custom-branding")
@Description("Changes the YouTube launcher icon and name to your choice (defaults to ReVanced).")
@CustomBrandingCompatibility
@Version("0.0.1")
class CustomBrandingPatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        val resDirectory = context["res"]
        if (!resDirectory.isDirectory) return PatchResultError("The res folder can not be found.")

        // Icon branding
        val iconNames = arrayOf(
            "adaptiveproduct_youtube_background_color_108",
            "adaptiveproduct_youtube_foreground_color_108",
            "ic_launcher",
            "ic_launcher_round"
        )

        mapOf(
            "xxxhdpi" to 192,
            "xxhdpi" to 144,
            "xhdpi" to 96,
            "hdpi" to 72,
            "mdpi" to 48
        ).forEach { (iconDirectory, size) ->
            iconNames.forEach { iconName ->
                val iconFile = getIconStream("branding/$size/$iconName.png")
                    ?: return PatchResultError("The icon $iconName can not be found.")

                Files.write(
                    resDirectory.resolve("mipmap-$iconDirectory").resolve("$iconName.png").toPath(),
                    iconFile.readBytes()
                )
            }
        }

        // Name branding
        val manifest = context["AndroidManifest.xml"]
        manifest.writeText(
            manifest.readText()
                .replace(
                    "android:label=\"@string/application_name",
                    "android:label=\"$appName"
                )
        )

        return PatchResultSuccess()
    }

    private fun getIconStream(iconPath: String): InputStream? {
        if (appIconPath == null) {
            return this.javaClass.classLoader.getResourceAsStream(iconPath)
        }
        val file = File(appIconPath!!).resolve(iconPath)
        if (!file.exists()) return null
        return FileInputStream(file)
    }

    companion object : OptionsContainer() {
        private var appName: String? by option(
            PatchOption.StringOption(
                key = "appName",
                default = "YouTube ReVanced",
                title = "Application Name",
                description = "The name of the application it will show on your home screen.",
                required = true
            )
        )

        private var appIconPath: String? by option(
            PatchOption.StringOption(
                key = "appIconPath",
                default = null,
                title = "Application Icon Path",
                description = "A path to the icon of the application."
            )
        )
    }
}
