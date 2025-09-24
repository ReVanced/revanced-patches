package app.revanced.patches.shared.layout.theme

import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.childElementsSequence
import org.w3c.dom.Document

const val PURE_BLACK_COLOR = "@android:color/black"
const val WHITE_COLOR = "@android:color/white"

val DARK_THEME_COLOR_VALUES = mapOf(
    "Pure black" to PURE_BLACK_COLOR,
    "Material You" to "@android:color/system_neutral1_900",
    "Classic (old YouTube)" to "#FF212121",
    "Catppuccin (Mocha)" to "#FF181825",
    "Dark pink" to "#FF290025",
    "Dark blue" to "#FF001029",
    "Dark green" to "#FF002905",
    "Dark yellow" to "#FF282900",
    "Dark orange" to "#FF291800",
    "Dark red" to "#FF290000",
)

val LIGHT_THEME_COLOR_VALUES = mapOf(
    "White" to WHITE_COLOR,
    "Material You" to "@android:color/system_neutral1_50",
    "Catppuccin (Latte)" to "#FFE6E9EF",
    "Light pink" to "#FFFCCFF3",
    "Light blue" to "#FFD1E0FF",
    "Light green" to "#FFCCFFCC",
    "Light yellow" to "#FFFDFFCC",
    "Light orange" to "#FFFFE6CC",
    "Light red" to "#FFFFD6D6",
)

fun baseThemePatch(
    block: BytecodePatchBuilder.() -> Unit = {},
    executeBlock: BytecodePatchContext.() -> Unit = {},
) = bytecodePatch(
    name = "Theme",
    description = "Adds options for theming and applies a custom background theme " +
            "(dark background theme defaults to pure black).",
) {
    block()

    execute {
        executeBlock()
    }
}

fun editThemeColors(document: Document, darkColor: String, lightColor: String? = null) {
    val resourcesNode = document.getElementsByTagName("resources").item(0)

    resourcesNode.childElementsSequence().forEach { node ->
        val name = node.getAttribute("name")
        when {
            name in setOf(
                "yt_black0", "yt_black1", "yt_black1_opacity95", "yt_black1_opacity98",
                "yt_black2", "yt_black3", "yt_black4", "yt_status_bar_background_dark",
                "material_grey_850"
            ) -> node.textContent = darkColor
            lightColor != null && name in setOf(
                "yt_white1", "yt_white1_opacity95", "yt_white1_opacity98",
                "yt_white2", "yt_white3", "yt_white4"
            ) -> node.textContent = lightColor
        }
    }
}
