package app.revanced.patches.reddit.layout.branding.name

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import java.io.FileWriter
import java.nio.file.Files

@Patch(
    name = "Custom branding name Reddit",
    description = "Renames the Reddit app to the name specified in options.json.",
    compatiblePackages = [CompatiblePackage("com.reddit.frontpage")],
    use = false
)
@Suppress("unused")
object CustomBrandingNamePatch : ResourcePatch() {
    private const val APP_NAME = "RVX Reddit"

    private val AppName by stringPatchOption(
        key = "AppName",
        default = APP_NAME,
        title = "App name",
        description = "The name of the app."
    )

    override fun execute(context: ResourceContext) {
        val appName = AppName
            ?: throw PatchException("Invalid app name.")

        val resDirectory = context["res"]

        val valuesV24Directory = resDirectory.resolve("values-v24")
        if (!valuesV24Directory.isDirectory)
            Files.createDirectories(valuesV24Directory.toPath())

        val stringsXml = valuesV24Directory.resolve("strings.xml")

        if (!stringsXml.exists()) {
            FileWriter(stringsXml).use {
                it.write("<?xml version=\"1.0\" encoding=\"utf-8\"?><resources></resources>")
            }
        }

        context.xmlEditor["res/values-v24/strings.xml"].use { editor ->
            val document = editor.file

            mapOf(
                "app_name" to appName
            ).forEach { (k, v) ->
                val stringElement = document.createElement("string")

                stringElement.setAttribute("name", k)
                stringElement.textContent = v

                document.getElementsByTagName("resources").item(0).appendChild(stringElement)
            }
        }
    }
}
