package app.revanced.patches.youtube.layout.branding.header.patch

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
import app.revanced.patches.youtube.layout.branding.header.annotations.PremiumHeadingCompatibility
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@Patch
@Dependencies(
    dependencies = [FixLocaleConfigErrorPatch::class]
)
@Name("premium-heading")
@Description("Show the premium branding on the the YouTube home screen.")
@PremiumHeadingCompatibility
@Version("0.0.1")
class PremiumHeadingPatch : ResourcePatch() {
    override fun execute(data: ResourceData): PatchResult {
        val resDirectory = data.get("res")
        if (!resDirectory.isDirectory) return PatchResultError("The res folder can not be found.")

        val (original, replacement) = "yt_premium_wordmark_header" to "yt_wordmark_header"
        val modes = arrayOf("light", "dark")

        arrayOf("xxxhdpi", "xxhdpi", "xhdpi", "hdpi", "mdpi").forEach { size ->
            val headingDirectory = resDirectory.resolve("drawable-$size")
            modes.forEach {mode ->
                Files.copy(
                    headingDirectory.resolve("${original}_$mode.png").toPath(),
                    headingDirectory.resolve("${replacement}_$mode.png").toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
        }

        return PatchResultSuccess()
    }
}
