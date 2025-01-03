package app.revanced.patches.music.layout.theme

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.shared.layout.theme.ThemePatch
import org.w3c.dom.Element

val themePatch = resourcePatch(
    name = "AMOLED theme",
    description = "Applies AMOLED black theme to YouTube Music.",
) {
    dependsOn(
        ThemePatch,
    )

    execute {
        val amoledBlackColor = "@android:color/black"

        document("res/values/colors.xml").use { document ->
            val resourcesNode = document.getElementsByTagName("resources").item(0) as Element

            for (i in 0 until resourcesNode.childNodes.length) {
                val node = resourcesNode.childNodes.item(i) as? Element ?: continue

                if (node.getAttribute("name") in listOf(
                        "yt_black0", "yt_black1", "yt_black1_opacity95", "yt_black1_opacity98",
                        "yt_black2", "yt_black3", "yt_black4", "yt_status_bar_background_dark",
                        "ytm_color_grey_12", "material_grey_850"
                    )) {
                    node.textContent = amoledBlackColor
                }
            }
        }
    }

    compatibleWith("com.google.android.apps.youtube.music"()
    )
}
