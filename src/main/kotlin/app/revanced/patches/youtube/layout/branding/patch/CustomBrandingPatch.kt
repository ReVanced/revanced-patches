package app.revanced.patches.youtube.layout.branding.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.implementation.ResourceData
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.implementation.ResourcePatch
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultError
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patches.youtube.layout.branding.annotations.CustomBrandingCompatibility
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch
import java.nio.file.Files

@Patch
@Dependencies(
    dependencies = [FixLocaleConfigErrorPatch::class]
)
@Name("custom-branding")
@Description("Change the branding of YouTube.")
@CustomBrandingCompatibility
@Version("0.0.1")
class CustomBrandingPatch : ResourcePatch() {
    override fun execute(data: ResourceData): PatchResult {
        val resDirectory = data.get("res")
        if (!resDirectory.isDirectory) return PatchResultError("The res folder can not be found.")

        val iconNames = arrayOf(
            "adaptiveproduct_youtube_background_color_108",
            "adaptiveproduct_youtube_foreground_color_108",
            "ic_launcher",
            "ic_launcher_round"
        )

        mapOf(
            "mipmap-xxxhdpi" to 192,
            "mipmap-xxhdpi" to 144,
            "mipmap-xhdpi" to 96,
            "mipmap-hdpi" to 72,
            "mipmap-mdpi" to 48
        ).forEach { (iconDirectory, size) ->
            iconNames.forEach iconLoop@{ iconName ->
                val iconFile = this.javaClass.classLoader.getResourceAsStream("branding/$size/$iconName.png")
                    ?: return PatchResultError("The icon $iconName can not be found.")

                Files.write(
                    resDirectory.resolve(iconDirectory).resolve("$iconName.png").toPath(), iconFile.readAllBytes()
                )
            }
        }

        return PatchResultSuccess()
    }
}
