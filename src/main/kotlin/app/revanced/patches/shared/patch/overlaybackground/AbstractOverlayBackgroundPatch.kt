package app.revanced.patches.shared.patch.overlaybackground

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.util.doRecursively
import org.w3c.dom.Element
import kotlin.io.path.exists

abstract class AbstractOverlayBackgroundPatch(
    private val files: Array<String>,
    private val targetId: Array<String>,
) : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        val resDirectory = context["res"]

        files.forEach { file ->
            val targetXmlPath = resDirectory.resolve("layout").resolve(file).toPath()

            if (targetXmlPath.exists()) {
                targetId.forEach { identifier ->
                    context.xmlEditor["res/layout/$file"].use { editor ->
                        editor.file.doRecursively {
                            arrayOf("height", "width").forEach replacement@{ replacement ->
                                if (it !is Element) return@replacement

                                if (it.attributes.getNamedItem("android:id")?.nodeValue?.endsWith(identifier) == true) {
                                    it.getAttributeNode("android:layout_$replacement")?.let { attribute -> attribute.textContent = "0.0dip" }
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}
