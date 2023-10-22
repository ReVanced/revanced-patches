package app.revanced.patches.youtube.layout.branding.header

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.booleanPatchOption
import kotlin.io.path.copyTo

@Patch(
    name = "Premium heading",
    description = "Show or hide the premium heading.",
    compatiblePackages = [
        CompatiblePackage("com.google.android.youtube")
    ]
)
@Suppress("unused")
object PremiumHeadingPatch : ResourcePatch() {
    private const val DEFAULT_HEADING_RES = "yt_wordmark_header"
    private const val PREMIUM_HEADING_RES = "yt_premium_wordmark_header"

    private val usePremiumHeading by booleanPatchOption(
        key = "usePremiumHeading",
        default = true,
        title = "Use premium heading",
        description = "Whether to use the premium heading.",
        required = true,
    )

    override fun execute(context: ResourceContext) {
        val resDirectory = context["res"]

        val (original, replacement) = if (usePremiumHeading!!)
            PREMIUM_HEADING_RES to DEFAULT_HEADING_RES
        else
            DEFAULT_HEADING_RES to PREMIUM_HEADING_RES

        val variants = arrayOf("light", "dark")

        arrayOf(
            "xxxhdpi",
            "xxhdpi",
            "xhdpi",
            "hdpi",
            "mdpi"
        ).mapNotNull { dpi ->
            resDirectory.resolve("drawable-$dpi").takeIf { it.exists() }?.toPath()
        }.also {
            if (it.isEmpty())
                throw PatchException("The drawable folder can not be found. Therefore, the patch can not be applied.")
        }.forEach { path ->

            variants.forEach { mode ->
                val fromPath = path.resolve("${original}_$mode.png")
                val toPath = path.resolve("${replacement}_$mode.png")

                fromPath.copyTo(toPath, true)
            }
        }
    }
}
