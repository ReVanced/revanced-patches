package app.revanced.patches.youtube.layout.branding.header.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.ResourceData
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.ResourcePatch
import app.revanced.patches.youtube.layout.branding.header.annotations.PremiumHeadingCompatibility
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists

@Patch
@Dependencies(
    dependencies = [FixLocaleConfigErrorPatch::class]
)
@Name("premium-heading")
@Description("Shows premium branding on the YouTube home screen.")
@PremiumHeadingCompatibility
@Version("0.0.1")
class PremiumHeadingPatch : ResourcePatch() {
    override fun execute(data: ResourceData): PatchResult {
        val resDirectory = data["res"]
        if (!resDirectory.isDirectory) return PatchResultError("The res folder can not be found.")

        val (original, replacement) = "yt_premium_wordmark_header" to "yt_wordmark_header"
        val modes = arrayOf("light", "dark")

        arrayOf("xxxhdpi", "xxhdpi", "xhdpi", "hdpi", "mdpi").forEach { size ->
            val headingDirectory = resDirectory.resolve("drawable-$size")
            modes.forEach {mode ->
                val fromPath = headingDirectory.resolve("${original}_$mode.png").toPath()
                val toPath = headingDirectory.resolve("${replacement}_$mode.png").toPath()

                if (!fromPath.exists())
                    return PatchResultError("The file $fromPath does not exist in the resources. Therefore, this patch can not succeed.")
                Files.copy(
                    fromPath,
                    toPath,
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
        }

        return PatchResultSuccess()
    }
}
