package app.revanced.patches.all.misc.packagename.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.*
import app.revanced.patcher.patch.annotations.Patch
import org.w3c.dom.Element

@Patch(false)
@Name("change-package-name")
@Description("Changes the package name.")
@Version("0.0.1")
class ChangePackageNamePatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        packageName?.let { packageName ->
            val packageNameRegex = Regex("^[a-z]\\w*(\\.[a-z]\\w*)+\$")
            if (!packageName.matches(packageNameRegex))
                return PatchResultError("Invalid package name")

            var originalPackageName = ""
            context.xmlEditor["AndroidManifest.xml"].use { editor ->
                val manifest = editor.file.getElementsByTagName("manifest").item(0) as Element
                originalPackageName = manifest.getAttribute("package")
            }

            if (!originalPackageName.matches(packageNameRegex))
                return PatchResultError("Failed to get the original package name")

            context["AndroidManifest.xml"].apply {
                readText().replace(originalPackageName, packageName).let(::writeText)
            }

        } ?: return PatchResultError("No package name provided")

        return PatchResultSuccess()
    }

    companion object : OptionsContainer() {
        var packageName: String? by option(
            PatchOption.StringOption(
                key = "packageName",
                default = "",
                title = "Package name",
                description = "The name of the package to rename of the app.",
            )
        )
    }
}