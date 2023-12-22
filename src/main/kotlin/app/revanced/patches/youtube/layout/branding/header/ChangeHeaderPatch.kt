package app.revanced.patches.youtube.layout.branding.header

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import app.revanced.util.inputStreamFromBundledResource
import java.awt.Graphics2D
import java.awt.GraphicsConfiguration
import java.awt.GraphicsEnvironment
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.awt.image.ColorConvertOp
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO

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
    private const val REVANCED_YOUTUBE_HEADER_NAME = "ReVanced YouTube"

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
        default = "ReVanced YouTube",
        values = mapOf(
            "YouTube" to HEADER_NAME,
            "YouTube Premium" to PREMIUM_HEADER_NAME,
            "ReVanced" to REVANCED_HEADER_NAME,
            "ReVanced YouTube" to REVANCED_YOUTUBE_HEADER_NAME,
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
        val targetResourceFiles = targetResourceDirectoryNames.map { directoryName ->
            ResourceGroup(
                directoryName,
                *variants.map { variant -> "${HEADER_NAME}_$variant.png" }.toTypedArray()
            )
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
            targetResourceFiles.forEach { context.copyResources("change-header/revanced", it) }

            // Overwrite the premium with the custom header as well.
            toHeader()
        }
        val toReVancedYouTube = {
            targetResourceFiles.forEach {
                it.resources.forEach { resource ->
                    val relativeFilePath = "${it.resourceDirectoryName}/$resource"
                    val targetFilePath = context["res"].resolve(relativeFilePath)
                    val overlayImage = inputStreamFromBundledResource(
                        "change-header/revanced-youtube", relativeFilePath)!!
                    applyOverlayImage(targetFilePath, overlayImage, targetFilePath)
                }
            }
            // Overwrite the premium with the custom header as well.
            toHeader()
        }
        val toCustom = {
            var foundImageResources = false
            // For all the resource groups in the custom header folder, copy them to the resource directories.
            File(header!!).listFiles { file -> file.isDirectory }?.forEach { folder ->
                val targetDirectory = context["res"].resolve(folder.name)
                // Skip if the target directory (DPI) doesn't exist.
                if (!targetDirectory.exists()) return@forEach

                folder.listFiles { file -> file.isFile }?.forEach {
                    val targetResourceFile = targetDirectory.resolve(it.name)

                    it.copyTo(targetResourceFile, true)
                }
                foundImageResources = true
            }

            if (!foundImageResources) throw PatchException("Could not find any custom images resources in directory: $header")

            // Overwrite the premium with the custom header as well.
            toHeader()
        }

        when (header) {
            HEADER_NAME -> toHeader
            PREMIUM_HEADER_NAME -> toPremium
            REVANCED_HEADER_NAME -> toReVanced
            REVANCED_YOUTUBE_HEADER_NAME -> toReVancedYouTube
            else -> toCustom
        }()
    }

    /**
     * Applies an overlay image to an original image.
     * Does not retain any content from the original image where the overlay overlaps
     * (even if the overlay replacement is transparent in those areas).
     */
    private fun applyOverlayImage(originalImageFile: File, overlayImageInput: InputStream, outputImageFile: File) {
        val originalImage = convertToARGB(ImageIO.read(originalImageFile))
        val overlayImage = convertToARGB(ImageIO.read(overlayImageInput))

        for (x in 0 until overlayImage.width) {
            for (y in 0 until overlayImage.height) {
                originalImage.setRGB(x, y, overlayImage.getRGB(x, y))
            }
        }

        ImageIO.write(originalImage, outputImageFile.extension, outputImageFile)
    }

    private fun convertToARGB(inputImage: BufferedImage): BufferedImage {
        val graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val graphicsDevice = graphicsEnvironment.defaultScreenDevice
        val graphicsConfiguration: GraphicsConfiguration = graphicsDevice.defaultConfiguration
        val argbImage = graphicsConfiguration.createCompatibleImage(
            inputImage.width,
            inputImage.height,
            Transparency.TRANSLUCENT
        )
        val g2d: Graphics2D = argbImage.createGraphics()

        val colorConvertOp =
            ColorConvertOp(inputImage.colorModel.colorSpace, argbImage.colorModel.colorSpace, null)
        colorConvertOp.filter(inputImage, argbImage)

        g2d.dispose()
        return argbImage
    }
}
