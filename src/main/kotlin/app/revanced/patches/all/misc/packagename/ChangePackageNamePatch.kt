package app.revanced.patches.all.misc.packagename

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.types.StringPatchOption.Companion.stringPatchOption
import org.w3c.dom.Element

@Patch(
    name = "Change package name",
    description = "Appends \".revanced\" to the package name by default.",
    use = false
)
@Suppress("unused")
object ChangePackageNamePatch : ResourcePatch() {
    private var packageName by stringPatchOption(
        key = "packageName",
        default = null,
        title = "Package name",
        description = "The name of the package to rename the app to.",
    )

    override fun execute(context: ResourceContext) {
        val packageNameToUse = packageName ?: getDefaultPackageName(context)

        val packageNameRegex = Regex("^[a-z]\\w*(\\.[a-z]\\w*)+\$")
        if (!packageNameToUse.matches(packageNameRegex))
            throw PatchException("Invalid package name")

        val originalPackageName = getOriginalPackageName(context)

        context["AndroidManifest.xml"].apply {
            readText().replace(originalPackageName, packageNameToUse).let(::writeText)
        }
    }

    private fun getDefaultPackageName(context: ResourceContext): String {
        val originalPackageName = getOriginalPackageName(context)
        return "$originalPackageName.revanced"
    }

    private fun getOriginalPackageName(context: ResourceContext): String {
        context.xmlEditor["AndroidManifest.xml"].use { editor ->
            val manifest = editor.file.getElementsByTagName("manifest").item(0) as Element
            return manifest.getAttribute("package")
        }
    }
}