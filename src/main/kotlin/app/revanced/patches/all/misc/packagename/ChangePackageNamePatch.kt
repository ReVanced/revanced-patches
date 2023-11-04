package app.revanced.patches.all.misc.packagename

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import org.w3c.dom.Element

@Patch(
    name = "Change package name",
    description = "Appends \".revanced\" to the package name by default.",
    use = false
)
@Suppress("unused")
object ChangePackageNamePatch : ResourcePatch() {
    private const val DEFAULT_PACKAGE_NAME_OPTION = "Default"

    private var packageName by stringPatchOption(
        key = "packageName",
        default = DEFAULT_PACKAGE_NAME_OPTION,
        values = mapOf("Default" to DEFAULT_PACKAGE_NAME_OPTION),
        title = "Package name",
        description = "The name of the package to rename the app to.",
        required = true
    ) {
        it == "Default" || it!!.matches(Regex("^[a-z]\\w*(\\.[a-z]\\w*)+\$"))
    }

    override fun execute(context: ResourceContext) {
        fun getOriginalPackageName(context: ResourceContext): String {
            context.xmlEditor["AndroidManifest.xml"].use { editor ->
                val manifest = editor.file.getElementsByTagName("manifest").item(0) as Element
                return manifest.getAttribute("package")
            }
        }

        val originalPackageName = getOriginalPackageName(context)
        if (packageName == DEFAULT_PACKAGE_NAME_OPTION) packageName = "$originalPackageName.revanced"

        context["AndroidManifest.xml"].apply {
            readText().replace(originalPackageName, packageName!!).let(::writeText)
        }
    }
}