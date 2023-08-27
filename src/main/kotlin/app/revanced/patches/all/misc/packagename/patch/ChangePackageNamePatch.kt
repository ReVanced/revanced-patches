package app.revanced.patches.all.misc.packagename.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.*
import app.revanced.patcher.patch.annotations.Patch
import org.w3c.dom.Element

@Patch(false)
@Name("Change package name")
@Description("Changes the package name. Appends \".revanced\" to the package name by default.")
class ChangePackageNamePatch : ResourcePatch {
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

    companion object : OptionsContainer() {
        var packageName: String? by option(
            PatchOption.StringOption(
                key = "packageName",
                default = null,
                title = "Package name",
                description = "The name of the package to rename the app to.",
            )
        )
    }
}