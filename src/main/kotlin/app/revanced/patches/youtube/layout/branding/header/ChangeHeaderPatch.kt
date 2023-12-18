package app.revanced.patches.youtube.layout.branding.header

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import java.io.File

@Patch(
    name = "Change header",
    description = "Change the header in top bar. Defaults to the ReVanced header.",
    compatiblePackages = [
        CompatiblePackage("com.google.android.youtube")
    ]
)
@Suppress("unused")
object ChangeHeaderPatch : ResourcePatch() {
    private const val HEADER_NAME = "yt_wordmark_header"
    private const val PREMIUM_HEADER_NAME = "yt_premium_wordmark_header"
    private const val REVANCED_HEADER_NAME = "ReVanced"

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
        default = "ReVanced",
        values = mapOf(
            "YouTube" to HEADER_NAME,
            "YouTube Premium" to PREMIUM_HEADER_NAME,
            "ReVanced" to REVANCED_HEADER_NAME,
        ),
        title = "Header",
        description = """
            The header to use in top bar or the path to a custom header.
            The path to a folder containing one or more of the following folders matching the DPI of your device:

            ${targetResourceDirectoryNames.joinToString("\n") { "- $it" }}

            These folders must contain the following files:

            ${variants.joinToString("\n") { variant -> "- ${HEADER_NAME}_$variant.png" }}
        """.trimIndent(),
        required = true,
    )

    override fun execute(context: ResourceContext) {
        val targetResourceDirectories = targetResourceDirectoryNames.mapNotNull {
            context["res"].resolve(it).takeIf(File::exists)
        }

        val overwriteFromTo: (String, String) -> Unit = { from: String, to: String ->
            targetResourceDirectories.forEach { directory ->
                variants.forEach { variant ->
                    val fromPath = directory.resolve("${from}_$variant.png")
                    val toPath = directory.resolve("${to}_$variant.png")

                    fromPath.copyTo(toPath, true)
                }
            }
        }

        val toPremium = { overwriteFromTo(PREMIUM_HEADER_NAME, HEADER_NAME) }
        val toHeader = { overwriteFromTo(HEADER_NAME, PREMIUM_HEADER_NAME) }
        val toReVanced = {
            // Copy the ReVanced header to the resource directories.
            targetResourceDirectoryNames.map { directoryName ->
                ResourceGroup(directoryName, *variants.map { variant -> "${HEADER_NAME}_$variant.png" }.toTypedArray())
            }.forEach { context.copyResources("change-header", it) }

            // Overwrite the premium with the custom header as well.
            toHeader()
        }
        val toCustom = {
            // For all the resource groups in the custom header folder, copy them to the resource directories.
            File(header!!).listFiles { file -> file.isDirectory }?.forEach { folder ->
                val targetDirectory = context["res"].resolve(folder.name)
                // Skip if the target directory (DPI) doesn't exist.
                if (!targetDirectory.exists()) return@forEach

                folder.listFiles { file -> file.isFile }?.forEach {
                    val targetResourceFile = targetDirectory.resolve(it.name)

                    it.copyTo(targetResourceFile, true)
                }
            }

            // Overwrite the premium with the custom header as well.
            toHeader()
        }

        when (header) {
            HEADER_NAME -> toHeader
            PREMIUM_HEADER_NAME -> toPremium
            REVANCED_HEADER_NAME -> toReVanced
            else -> toCustom
        }()
    }
}
