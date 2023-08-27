package app.revanced.patches.youtube.layout.branding.header.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.layout.branding.header.annotations.PremiumHeadingCompatibility
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists

@Patch
@Name("Premium heading")
@Description("Shows premium branding on the home screen.")
@PremiumHeadingCompatibility
class PremiumHeadingPatch : ResourcePatch {
    override fun execute(context: ResourceContext) {
        val resDirectory = context["res"]
        if (!resDirectory.isDirectory) throw PatchException("The res folder can not be found.")

        val (original, replacement) = "yt_premium_wordmark_header" to "yt_wordmark_header"
        val modes = arrayOf("light", "dark")

        arrayOf("xxxhdpi", "xxhdpi", "xhdpi", "hdpi", "mdpi").forEach { size ->
            val headingDirectory = resDirectory.resolve("drawable-$size")
            modes.forEach { mode ->
                val fromPath = headingDirectory.resolve("${original}_$mode.png").toPath()
                val toPath = headingDirectory.resolve("${replacement}_$mode.png").toPath()

                if (!fromPath.exists())
                    throw PatchException("The file $fromPath does not exist in the resources. Therefore, this patch can not succeed.")
                Files.copy(
                    fromPath,
                    toPath,
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
        }
    }
}
