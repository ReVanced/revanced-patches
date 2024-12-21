package app.revanced.patches.youtube.layout.player.background

import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.doRecursively
import org.w3c.dom.Element

@Suppress("unused")
val playerControlsBackgroundPatch = resourcePatch(
    name = "Remove player controls background",
    description = "Removes the dark background surrounding the video player controls.",
    use = false,
) {
    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.45.38",
            "19.46.42",
            "19.47.53",
        ),
    )

    execute {
        document("res/drawable/player_button_circle_background.xml").use { document ->

            document.doRecursively node@{ node ->
                if (node !is Element) return@node

                node.getAttributeNode("android:color")?.let { attribute ->
                    attribute.textContent = "@android:color/transparent"
                }
            }
        }
    }
}
