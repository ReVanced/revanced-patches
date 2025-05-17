package app.revanced.patches.youtube.layout.branding.header

import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.youtube.misc.playservice.is_19_25_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.Utils.trimIndentMultiline
import app.revanced.util.copyResources
import app.revanced.util.findElementByAttributeValueOrThrow
import java.io.File

private const val HEADER_FILE_NAME = "yt_wordmark_header"
private const val PREMIUM_HEADER_FILE_NAME = "yt_premium_wordmark_header"

private const val HEADER_OPTION = "header*"
private const val PREMIUM_HEADER_OPTION = "premium*header"
private const val REVANCED_HEADER_OPTION = "revanced*"
private const val REVANCED_BORDERLESS_HEADER_OPTION = "revanced*borderless"

private val targetResourceDirectoryNames = arrayOf(
    "xxxhdpi" to "512px x 192px",
    "xxhdpi" to "387px x 144px",
    "xhdpi" to "258px x 96px",
    "hdpi" to "194px x 72px",
    "mdpi" to "129px x 48px",
).associate { (dpi, dim) ->
    "drawable-$dpi" to dim
}

private val variants = arrayOf("light", "dark")

@Suppress("unused")
val changeHeaderPatch = resourcePatch(
    name = "Change header",
    description = "Applies a custom header in the top left corner within the app. Defaults to the ReVanced header.",
    use = false,
) {
    dependsOn(versionCheckPatch)

    compatibleWith(
        "com.google.android.youtube"(
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.47.53",
            "20.07.39",
            "20.12.46",
        )
    )

    val header by stringOption(
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

    execute {
        // The directories to copy the header to.
        val targetResourceDirectories = targetResourceDirectoryNames.keys.mapNotNull {
            get("res").resolve(it).takeIf(File::exists)
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
        fun overwriteFromTo(from: String, to: String) {
            targetResourceDirectories.forEach { directory ->
                variants.forEach { variant ->
                    val fromPath = directory.resolve("${from}_$variant.png")
                    val toPath = directory.resolve("${to}_$variant.png")

                    fromPath.copyTo(toPath, true)
                }
            }
        }

        // Functions to overwrite the header to the different variants.
        fun toPremium() { overwriteFromTo(PREMIUM_HEADER_FILE_NAME, HEADER_FILE_NAME) }
        fun toHeader() { overwriteFromTo(HEADER_FILE_NAME, PREMIUM_HEADER_FILE_NAME) }
        fun toReVanced() {
            // Copy the ReVanced header to the resource directories.
            targetResourceFiles.forEach { copyResources("change-header/revanced", it) }

            // Overwrite the premium with the custom header as well.
            toHeader()
        }
        fun toReVancedBorderless() {
            // Copy the ReVanced borderless header to the resource directories.
            targetResourceFiles.forEach {
                copyResources(
                    "change-header/revanced-borderless",
                    it
                )
            }

            // Overwrite the premium with the custom header as well.
            toHeader()
        }
        fun toCustom() {
            val sourceFolders = File(header!!).listFiles { file -> file.isDirectory }
                ?: throw PatchException("The provided path is not a directory: $header")

            var copiedFiles = false

            // For each source folder, copy the files to the target resource directories.
            sourceFolders.forEach { dpiSourceFolder ->
                val targetDpiFolder = get("res").resolve(dpiSourceFolder.name)
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
            HEADER_OPTION -> toHeader()
            PREMIUM_HEADER_OPTION -> toPremium()
            REVANCED_HEADER_OPTION -> toReVanced()
            REVANCED_BORDERLESS_HEADER_OPTION -> toReVancedBorderless()
            else -> toCustom()
        }

        // Fix 19.25+ A/B layout with different header icons:
        // yt_ringo2_wordmark_header, yt_ringo2_premium_wordmark_header
        //
        // These images are webp and not png, so overwriting them is not so simple.
        // Instead change styles.xml to use the old drawable resources.
        if (is_19_25_or_greater) {
            document("res/values/styles.xml").use { document ->
                val documentChildNodes = document.childNodes

                arrayOf(
                    "CairoLightThemeRingo2Updates" to variants[0],
                    "CairoDarkThemeRingo2Updates" to variants[1]
                ).forEach { (styleName, theme) ->
                    val styleNodes = documentChildNodes.findElementByAttributeValueOrThrow(
                        "name",
                        styleName,
                    ).childNodes

                    val drawable = "@drawable/${HEADER_FILE_NAME}_${theme}"

                    arrayOf(
                        "ytWordmarkHeader",
                        "ytPremiumWordmarkHeader"
                    ).forEach { itemName ->
                        styleNodes.findElementByAttributeValueOrThrow(
                            "name",
                            itemName,
                        ).textContent = drawable
                    }
                }
            }
        }
    }
}
