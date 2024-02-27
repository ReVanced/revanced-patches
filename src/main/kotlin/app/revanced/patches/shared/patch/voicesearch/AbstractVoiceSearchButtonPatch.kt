package app.revanced.patches.shared.patch.voicesearch

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import org.w3c.dom.Element
import kotlin.io.path.exists

abstract class AbstractVoiceSearchButtonPatch(
    private val paths: Array<String>,
    private val replacements: Array<String>
) : ResourcePatch() {
    private companion object {
        const val IMAGE_VIEW_TAG = "android.support.v7.widget.AppCompatImageView"
        const val VOICE_SEARCH_ID = "@id/voice_search"
    }

    override fun execute(context: ResourceContext) {
        val resDirectory = context["res"]

        paths.forEach { path ->
            val targetXmlPath = resDirectory.resolve("layout").resolve(path).toPath()

            if (targetXmlPath.exists()) {
                context.xmlEditor["res/layout/$path"].use { editor ->
                    val document = editor.file
                    val imageViewTags = document.getElementsByTagName(IMAGE_VIEW_TAG)
                    List(imageViewTags.length) { imageViewTags.item(it) as Element }
                        .filter { it.getAttribute("android:id").equals(VOICE_SEARCH_ID) }
                        .forEach { node ->
                            replacements.forEach replacement@{ replacement ->
                                node.getAttributeNode("android:layout_$replacement")
                                    ?.let { attribute ->
                                        attribute.textContent = "0.0dip"
                                    }
                            }
                        }
                }
            }
        }

    }
}
