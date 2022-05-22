package app.revanced.patches.youtube.layout.amoled.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.implementation.ResourceData
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.implementation.ResourcePatch
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patches.youtube.layout.amoled.annotations.AmoledCompatibility
import org.w3c.dom.Element
import java.io.File

@Patch
@Name("amoled")
@Description("Enables black theme (amoled mode)")
@AmoledCompatibility
@Version("0.0.1")
class AmoledPatch : ResourcePatch() {
    override fun execute(data: ResourceData): PatchResult {
        data.getXmlEditor("res${File.separator}values${File.separator}colors.xml").use { editor ->
            val resourcesNode = editor.file.getElementsByTagName("resources").item(0) as Element

            for (i in 0..resourcesNode.childNodes.length) {
                val node = resourcesNode.childNodes.item(i) as Element

                node.nodeValue = when (node.getAttribute("name")) {
                    "yt_black1",
                    "yt_black1_opacity95",
                    "yt_black2",
                    "yt_black3",
                    "yt_black4",
                    "yt_status_bar_background_dark"
                    -> "@android:color/black"
                    "yt_selected_nav_label_dark"
                    -> "#ffdf0000"
                    else -> continue
                }
            }
        }

        return PatchResultSuccess()
    }
}
