package app.revanced.patches.youtube.layout.header

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import app.revanced.patches.youtube.utils.settings.ResourceUtils.updatePatchStatusHeader
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import java.io.File

@Patch(
    name = "Custom branding heading",
    description = "Applies a custom heading in the top left corner within the app.",
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
    ],
)
@Suppress("unused")

object CustomBrandingHeadingPatch : ResourcePatch() {
    private const val DEFAULT_HEADING_NAME = "yt_wordmark_header"
    private const val PREMIUM_HEADING_NAME = "yt_premium_wordmark_header"

    private val availableHeading = mapOf(
        "YouTube" to DEFAULT_HEADING_NAME,
        "YouTube Premium" to PREMIUM_HEADING_NAME,
    )

    private val targetResourceDirectoryNames = mapOf(
        "xxxhdpi" to "512px x 192px",
        "xxhdpi" to "387px x 144px",
        "xhdpi" to "258px x 96px",
        "hdpi" to "194px x 72px",
        "mdpi" to "129px x 48px",
    ).map { (dpi, dim) ->
        "drawable-$dpi" to dim
    }.toMap()

    private val variants = arrayOf("light", "dark")

    private val header by stringPatchOption(
        key = "header",
        default = PREMIUM_HEADING_NAME,
        values = availableHeading,
        title = "Header",
        description = """
            The header to apply to the app.
            
            If a path to a folder is provided, the folder must contain one or more of the following folders, depending on the DPI of the device:
            
            ${targetResourceDirectoryNames.keys.joinToString("\n") { "- $it" }}
            
            Each of the folders must contain all of the following files:
            
            ${variants.joinToString("\n") { variant -> "- ${DEFAULT_HEADING_NAME}_$variant.png" }}
            The image dimensions must be as follows:
            ${targetResourceDirectoryNames.map { (dpi, dim) -> "- $dpi: $dim" }.joinToString("\n")}
        """
        .split("\n")
        .joinToString("\n") { it.trimIndent() } // Remove the leading whitespace from each line.
        .trimIndent(), // Remove the leading newline.,
    )

    override fun execute(context: ResourceContext) {
        context.updatePatchStatusHeader("Default")

        // The directories to copy the header to.
        val targetResourceDirectories = targetResourceDirectoryNames.keys.mapNotNull {
            context["res"].resolve(it).takeIf(File::exists)
        }

        /**
         * A function that overwrites both header variants from [from] to [to] in the target resource directories.
         */
        val overwriteFromTo: (String, String) -> Unit = { from: String, to: String ->
            targetResourceDirectories.forEach { directory ->
                variants.forEach { variant ->
                    val fromPath = directory.resolve("${from}_$variant.png")
                    val toPath = directory.resolve("${to}_$variant.png")

                    fromPath.copyTo(toPath, true)
                }
            }
        }

        // Functions to overwrite the header to the different variants.
        val toPremium = {
            overwriteFromTo(PREMIUM_HEADING_NAME, DEFAULT_HEADING_NAME)
            context.updatePatchStatusHeader("Premium")
        }

        val toHeader = {
            overwriteFromTo(DEFAULT_HEADING_NAME, PREMIUM_HEADING_NAME)
        }

        val toCustom = {
            val sourceFolders = File(header!!).listFiles { file -> file.isDirectory }
                ?: throw PatchException("The provided path is not a directory: $header")

            var copiedFiles = false

            // For each source folder, copy the files to the target resource directories.
            sourceFolders.forEach { dpiSourceFolder ->
                val targetDpiFolder = context["res"].resolve(dpiSourceFolder.name)
                if (!targetDpiFolder.exists()) return@forEach

                val imgSourceFiles = dpiSourceFolder.listFiles { file -> file.isFile }!!
                imgSourceFiles.forEach { imgSourceFile ->
                    val imgTargetFile = targetDpiFolder.resolve(imgSourceFile.name)
                    imgSourceFile.copyTo(imgTargetFile, true)

                    copiedFiles = true
                }
            }

            if (!copiedFiles) {
                throw PatchException("No header files were copied from the provided path: $header.")
            }

            // Overwrite the premium with the custom header as well.
            toHeader()

            context.updatePatchStatusHeader("Custom")
        }

        when (header) {
            DEFAULT_HEADING_NAME -> toHeader
            PREMIUM_HEADING_NAME -> toPremium
            else -> toCustom
        }()
    }
}