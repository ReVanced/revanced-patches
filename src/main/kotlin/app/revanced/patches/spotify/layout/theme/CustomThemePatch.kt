package app.revanced.patches.spotify.layout.theme

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.types.StringPatchOption.Companion.stringPatchOption
import org.w3c.dom.Element

@Patch(
    name = "Custom theme",
    description = "Applies a custom theme.",
    compatiblePackages = [CompatiblePackage("com.spotify.music")]
)
@Suppress("unused")
object CustomThemePatch : ResourcePatch() {
    private var backgroundColor by stringPatchOption(
        key = "backgroundColor",
        default = "@android:color/black",
        title = "Background color",
        description = "The background color. Can be a hex color or a resource reference.",
    )

    private var accentColor by stringPatchOption(
        key = "accentColor",
        default = "#ff1ed760",
        title = "Accent color",
        description = "The accent color ('spotify green' by default). Can be a hex color or a resource reference.",
    )

    private var accentPressedColor by stringPatchOption(
        key = "accentPressedColor",
        default = "#ff169c46",
        title = "Pressed accent for the dark theme",
        description = "The color when accented buttons are pressed, by default slightly darker than accent. Can be a hex color or a resource reference."
    )

    override fun execute(context: ResourceContext) {
        context.xmlEditor["res/values/colors.xml"].use { editor ->
            val resourcesNode = editor.file.getElementsByTagName("resources").item(0) as Element

            for (i in 0 until resourcesNode.childNodes.length) {
                val node = resourcesNode.childNodes.item(i) as? Element ?: continue

                node.textContent = when (node.getAttribute("name")) {
                    "gray_7" -> backgroundColor!!
                    "dark_brightaccent_background_base", "dark_base_text_brightaccent", "green_light" -> accentColor!!
                    "dark_brightaccent_background_press" -> accentPressedColor!!
                    else -> continue
                }
            }
        }
    }
}