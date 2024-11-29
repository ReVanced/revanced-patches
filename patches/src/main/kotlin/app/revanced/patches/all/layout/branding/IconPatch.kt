package app.revanced.patches.all.layout.branding

import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatchContext
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patcher.util.Document
import app.revanced.util.getNode
import java.io.File
import java.io.FilenameFilter

val changeIconPatch = resourcePatch(
    name = "Change icon",
    description = "Changes the app icon to a custom icon. By default, the \"ReVanced icon\" is used.",
    use = false,
) {
    val revancedIconOptionValue = "" // Empty value == ReVanced icon.

    val pixelDensities = setOf(
        "xxxhdpi",
        "xxhdpi",
        "xhdpi",
        "hdpi",
        "mdpi",
    )

    val iconOptions = buildMap {
        arrayOf("foreground", "background", "monochrome").forEach { iconType ->
            this += pixelDensities.associateBy {
                stringOption(
                    key = "${iconType}IconPath",
                    default = revancedIconOptionValue,
                    values = mapOf("ReVanced Logo" to revancedIconOptionValue),
                    title = "Icon file path (Pixel density: $it, Icon type: $iconType)",
                    description = "The path to the icon file to apply to the app for the pixel density $it " +
                        "and icon type $iconType.",
                )
            }
        }

        // This might confuse the user.
        put(
            "full",
            stringOption(
                key = "fullIconPath",
                default = revancedIconOptionValue,
                values = mapOf("ReVanced Logo" to revancedIconOptionValue),
                title = "Full icon file path",
                description = "The path to the icon file to apply when the app " +
                    "does not have a specific icon for the pixel density.",
            ),
        )
    }

    execute {
        manifest {
            val applicationNode = getNode("application")
            val iconResourceReference = applicationNode.attributes.getNamedItem("android:icon").textContent!!

            val iconResourceFiles = resolve(iconResourceReference)

            iconResourceFiles.forEach { resourceFile ->
                if (resourceFile.extension == "xml" && resourceFile.name.startsWith("ic_launcher")) {
                    val adaptiveIcon = parseAdaptiveIcon(resourceFile)

                    // TODO: Replace the background, foreground, and monochrome icons with the custom icons.
                } else {
                    // TODO: Replace the icon with fullIcon.
                }
            }
        }
    }
}

context(ResourcePatchContext)
fun <T> manifest(block: Document.() -> T) = document("AndroidManifest.xml").use(block)

context(ResourcePatchContext)
private fun resolve(resourceReference: String): List<File> {
    val isMipmap = resourceReference.startsWith("@mipmap/")
    val isDrawable = resourceReference.startsWith("@drawable/")

    val directories = get("res").listFiles(
        if (isMipmap) {
            FilenameFilter { _, name -> name.startsWith("mipmap-") }
        } else if (isDrawable) {
            FilenameFilter { _, name -> name.startsWith("drawable-") }
        } else {
            throw PatchException("Unsupported resource reference: $resourceReference")
        },
    )!!

    // The name does not have an extension. It is the name of the resource.
    val resourceName = resourceReference.split("/").last()
    val resources = directories.mapNotNull {
        // Find the first file that starts with the resource name.
        it.listFiles { _, name -> name.startsWith(resourceName) }!!.firstOrNull()
    }

    return resources
}

private class IconResource(
    val file: File,
    val pixelDensity: String,
)

context(ResourcePatchContext)
private fun parseAdaptiveIcon(xmlFile: File) = document(xmlFile.absolutePath).use { adaptiveIconNode ->
    val adaptiveIcon = adaptiveIconNode.getNode("adaptive-icon")

    fun getIconResourceReference(iconType: String): List<IconResource>? {
        val resourceReferenceString = adaptiveIcon.getNode(iconType)?.let {
            it.attributes.getNamedItem("android:drawable").textContent!!
        }

        if (resourceReferenceString == null) {
            return null
        }

        return resolve(resourceReferenceString).map {
            IconResource(file = it, pixelDensity = it.parentFile.name.split("-").last())
        }
    }

    AdaptiveIcon(
        getIconResourceReference("background")!!,
        getIconResourceReference("foreground")!!,
        getIconResourceReference("monochrome"),
    )
}

private class AdaptiveIcon(
    val background: List<IconResource>,
    val foreground: List<IconResource>,
    val monochrome: List<IconResource>?,
)
