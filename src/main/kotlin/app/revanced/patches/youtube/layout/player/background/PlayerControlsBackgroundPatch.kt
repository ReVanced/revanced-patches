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
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39",
                "19.03.36",
                "19.04.38",
                "19.05.36",
                "19.06.39",
                "19.07.40",
                "19.08.36",
                "19.09.38",
                "19.10.39",
                "19.11.43"
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
