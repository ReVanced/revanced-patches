package app.revanced.patches.all.misc.packagename

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.patcher.patch.options.PatchOptionException
import org.w3c.dom.Element
import java.io.Closeable

@Patch(
    name = "Change package name",
    description = "Appends \".revanced\" to the package name by default. Changing the package name of the app can lead to unexpected issues.",
    use = false,
)
@Suppress("unused")
object ChangePackageNamePatch : ResourcePatch(), Closeable {
    private val packageNameOption =
        stringPatchOption(
            key = "packageName",
            default = "Default",
            values = mapOf("Default" to "Default"),
            title = "Package name",
            description = "The name of the package to rename the app to.",
            required = true,
        ) {
            it == "Default" || it!!.matches(Regex("^[a-z]\\w*(\\.[a-z]\\w*)+\$"))
        }

    private lateinit var context: ResourceContext

    override fun execute(context: ResourceContext) {
        this.context = context
    }

    /**
     * Set the package name to use.
     * If this is called multiple times, the first call will set the package name.
     *
     * @param fallbackPackageName The package name to use if the user has not already specified a package name.
     * @return The package name that was set.
     * @throws PatchOptionException.ValueValidationException If the package name is invalid.
     */
    fun setOrGetFallbackPackageName(fallbackPackageName: String): String {
        val packageName = packageNameOption.value!!

        return if (packageName == packageNameOption.default) {
            fallbackPackageName.also { packageNameOption.value = it }
        } else {
            packageName
        }
    }

    override fun close() =
        context.xmlEditor["AndroidManifest.xml"].use { editor ->
            val document = editor.file

            val replacementPackageName = packageNameOption.value

            val manifest = document.getElementsByTagName("manifest").item(0) as Element
            manifest.setAttribute(
                "package",
                if (replacementPackageName != packageNameOption.default) {
                    replacementPackageName
                } else {
                    "${manifest.getAttribute("package")}.revanced"
                },
            )
        }
}
