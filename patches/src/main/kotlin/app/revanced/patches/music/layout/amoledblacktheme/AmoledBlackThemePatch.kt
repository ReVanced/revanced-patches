package app.revanced.patches.music.layout.amoledblacktheme

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.smali.toInstructions
import org.w3c.dom.Element

val amoledBlackThemePatch = bytecodePatch(
    name = "AMOLED black theme",
    description = "Applies an AMOLED black theme for YouTube Music.",
) {
    val amoledBlackColor = "@android:color/black"

    dependsOn(
        resourcePatch {
            execute {
                document("res/values/colors.xml").use { document ->
                    val resourcesNode = document.getElementsByTagName("resources").item(0) as Element

                    for (i in 0 until resourcesNode.childNodes.length) {
                        val node = resourcesNode.childNodes.item(i) as? Element ?: continue

                        node.textContent = when (node.getAttribute("name")) {
                            "yt_black0", "yt_black1", "yt_black1_opacity95", "yt_black1_opacity98", "yt_black2", "yt_black3",
                            "yt_black4", "yt_status_bar_background_dark", "ytm_color_grey_12", "material_grey_850" -> amoledBlackColor

                            else -> continue
                        }
                    }
                }

                // Set the navigation bar color to black
                document("res/values/colors.xml").use { document ->
                    val resourcesNode = document.getElementsByTagName("resources").item(0) as Element

                    resourcesNode.appendChild(
                        document.createElement("color").apply {
                            setAttribute("name", "navigation_bar_color")
                            textContent = amoledBlackColor
                        }
                    )
                }
            }
        }
    )

    compatibleWith("com.google.android.apps.youtube.music")

    execute {
        // Modify the dark theme color
        themeHelperFingerprint.method.apply {
            val instructions = """
                const-string v0, "$amoledBlackColor"
                return-object v0
            """.toInstructions()

            addInstructions(themeHelperFingerprint.patternMatch!!.startIndex, instructions)
        }

        // Modify the navigation bar color
        themeHelperFingerprint.method.apply {
            val instructions = """
                const-string v0, "$amoledBlackColor"
                return-object v0
            """.toInstructions()

            addInstructions(themeHelperFingerprint.patternMatch!!.startIndex, instructions)
        }
    }
}
