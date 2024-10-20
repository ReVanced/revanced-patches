package app.revanced.patches.youtube.layout.player.background

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.util.doRecursively
import org.w3c.dom.Element

@Patch(
    name = "Remove player controls background",
    description = "Removes the dark background surrounding the video player controls.",
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.38.44",
                "18.49.37",
                "19.16.39",
                "19.25.37",
                "19.34.42",
            ],
        ),
    ],
    use = false,
)
@Suppress("unused")
object PlayerControlsBackgroundPatch : ResourcePatch() {
    private const val RESOURCE_FILE_PATH = "res/drawable/player_button_circle_background.xml"

    override fun execute(context: ResourceContext) {
        context.xmlEditor[RESOURCE_FILE_PATH].use { editor ->
            val document = editor.file

            document.doRecursively node@{ node ->
                if (node !is Element) return@node

                node.getAttributeNode("android:color")?.let { attribute ->
                    attribute.textContent = "@android:color/transparent"
                }
            }
        }
    }
}
