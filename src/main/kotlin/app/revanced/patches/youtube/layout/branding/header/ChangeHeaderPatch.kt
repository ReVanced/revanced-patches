package app.revanced.patches.youtube.layout.branding.header

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.util.ResourceGroup
import app.revanced.util.Utils.trimIndentMultiline
import app.revanced.util.copyResources
import java.io.File

@Patch(
    name = "Change header",
    description = "Applies a custom header in the top left corner within the app. Defaults to the ReVanced header.",
    compatiblePackages = [
        CompatiblePackage("com.google.android.youtube"),
    ],
    use = false,
)
@Suppress("unused")
object ChangeHeaderPatch : ResourcePatch() {
    private const val HEADER_FILE_NAME = "yt_wordmark_header"
    private const val PREMIUM_HEADER_FILE_NAME = "yt_premium_wordmark_header"

    private const val HEADER_OPTION = "header*"
    private const val PREMIUM_HEADER_OPTION = "premium*header"
    private const val REVANCED_HEADER_OPTION = "revanced*"
    private const val REVANCED_BORDERLESS_HEADER_OPTION = "revanced*borderless"

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
        default = REVANCED_BORDERLESS_HEADER_OPTION,
        values = mapOf(
            "YouTube" to HEADER_OPTION,
            "YouTube Premium" to PREMIUM_HEADER_OPTION,
            "ReVanced" to REVANCED_HEADER_OPTION,
            "ReVanced (borderless logo)" to REVANCED_BORDERLESS_HEADER_OPTION,
        ),
        title = "Header",
        description = """
            The header to apply to the app.
            
            If a path to a folder is provided, the folder must contain one or more of the following folders, depending on the DPI of the device:
            
            ${targetResourceDirectoryNames.keys.joinToString("\n") { "- $it" }}
            
            Each of the folders must contain all of the following files:
            
            ${variants.joinToString("\n") { variant -> "- ${HEADER_FILE_NAME}_$variant.png" }}

            The image dimensions must be as follows:
            ${targetResourceDirectoryNames.map { (dpi, dim) -> "- $dpi: $dim" }.joinToString("\n")}
        """.trimIndentMultiline(),
        required = true,
    )

    override fun execute(context: ResourceContext) {
        // The directories to copy the header to.
        val targetResourceDirectories = targetResourceDirectoryNames.keys.mapNotNull {
            context.get("res").resolve(it).takeIf(File::exists)
        }
        // The files to replace in the target directories.
        val targetResourceFiles = targetResourceDirectoryNames.keys.map { directoryName ->
            ResourceGroup(
                directoryName,
                *variants.map { variant -> "${HEADER_FILE_NAME}_$variant.png" }.toTypedArray(),
            )
        }

        /**
         * A function that overwrites both header variants in the target resource directories.
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
        val toPremium = { overwriteFromTo(PREMIUM_HEADER_FILE_NAME, HEADER_FILE_NAME) }
        val toHeader = { overwriteFromTo(HEADER_FILE_NAME, PREMIUM_HEADER_FILE_NAME) }
        val toReVanced = {
            // Copy the ReVanced header to the resource directories.
            targetResourceFiles.forEach { context.copyResources("change-header/revanced", it) }

            // Overwrite the premium with the custom header as well.
            toHeader()
        }
        val toReVancedBorderless = {
            // Copy the ReVanced borderless header to the resource directories.
            targetResourceFiles.forEach { context.copyResources("change-header/revanced-borderless", it) }

            // Overwrite the premium with the custom header as well.
            toHeader()
        }
        val toCustom = {
            val sourceFolders = File(header!!).listFiles { file -> file.isDirectory }
                ?: throw PatchException("The provided path is not a directory: $header")

            var copiedFiles = false

            // For each source folder, copy the files to the target resource directories.
            sourceFolders.forEach { dpiSourceFolder ->
                val targetDpiFolder = context.get("res").resolve(dpiSourceFolder.name)
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
        }

        when (header) {
            HEADER_OPTION -> toHeader
            PREMIUM_HEADER_OPTION -> toPremium
            REVANCED_HEADER_OPTION -> toReVanced
            REVANCED_BORDERLESS_HEADER_OPTION -> toReVancedBorderless
            else -> toCustom
        }()
    }
}
