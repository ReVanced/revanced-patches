package app.revanced.patches.youtube.layout.branding.header

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import java.io.File

@Patch(
    name = "Change header",
    description = "Applies a custom header in the top left corner within the app. Defaults to the ReVanced header.",
    compatiblePackages = [
        CompatiblePackage("com.google.android.youtube")
    ],
    use = false
)
@Suppress("unused")
object ChangeHeaderPatch : ResourcePatch() {
    private const val HEADER_NAME = "yt_wordmark_header"
    private const val PREMIUM_HEADER_NAME = "yt_premium_wordmark_header"
    private const val REVANCED_HEADER_NAME = "ReVanced"
    private const val REVANCED_BORDERLESS_HEADER_NAME = "ReVanced (borderless logo)"

    private val targetResourceDirectoryNames = arrayOf(
        "xxxhdpi",
        "xxhdpi",
        "xhdpi",
        "mdpi",
        "hdpi",
    ).map { dpi ->
        "drawable-$dpi"
    }

    private val variants = arrayOf("light", "dark")

    private val header by stringPatchOption(
        key = "header",
        default = REVANCED_BORDERLESS_HEADER_NAME,
        values = mapOf(
            "YouTube" to HEADER_NAME,
            "YouTube Premium" to PREMIUM_HEADER_NAME,
            "ReVanced" to REVANCED_HEADER_NAME,
            "ReVanced (borderless logo)" to REVANCED_BORDERLESS_HEADER_NAME,
        ),
        title = "Header",
        description = """
            Either a header name or a path to a custom header folder to use in the top bar.
            The path to a folder must contain one or more of the following folders matching the DPI of your device:

            ${targetResourceDirectoryNames.joinToString("\n") { "- $it" }}

            These folders must contain the following files:

            ${variants.joinToString("\n") { variant -> "- ${HEADER_NAME}_$variant.png" }}
        """.trimIndent(),
        required = true,
    )

    override fun execute(context: ResourceContext) {
        // The directories to copy the header to.
        val targetResourceDirectories = targetResourceDirectoryNames.mapNotNull {
            context["res"].resolve(it).takeIf(File::exists)
        }
        // The files to replace in the target directories.
        val targetResourceFiles = targetResourceDirectoryNames.map { directoryName ->
            ResourceGroup(
                directoryName,
                *variants.map { variant -> "${HEADER_NAME}_$variant.png" }.toTypedArray()
            )
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
        val toPremium = { overwriteFromTo(PREMIUM_HEADER_NAME, HEADER_NAME) }
        val toHeader = { overwriteFromTo(HEADER_NAME, PREMIUM_HEADER_NAME) }
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
            var copiedReplacementImages = false
            // For all the resource groups in the custom header folder, copy them to the resource directories.
            File(header!!).listFiles { file -> file.isDirectory }?.forEach { folder ->
                val targetDirectory = context["res"].resolve(folder.name)
                // Skip if the target directory (DPI) doesn't exist.
                if (!targetDirectory.exists()) return@forEach

                folder.listFiles { file -> file.isFile }?.forEach {
                    val targetResourceFile = targetDirectory.resolve(it.name)

                    it.copyTo(targetResourceFile, true)
                    copiedReplacementImages = true
                }
            }

            if (!copiedReplacementImages) throw PatchException("Could not find any custom images resources in directory: $header")

            // Overwrite the premium with the custom header as well.
            toHeader()
        }

        when (header) {
            HEADER_NAME -> toHeader
            PREMIUM_HEADER_NAME -> toPremium
            REVANCED_HEADER_NAME -> toReVanced
            REVANCED_BORDERLESS_HEADER_NAME -> toReVancedBorderless
            else -> toCustom
        }()
    }
}
