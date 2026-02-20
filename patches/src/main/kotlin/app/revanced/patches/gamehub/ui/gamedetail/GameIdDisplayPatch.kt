package app.revanced.patches.gamehub.ui.gamedetail

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.gamehub.misc.extension.sharedGamehubExtensionPatch
import app.revanced.util.asSequence
import org.w3c.dom.Element

private const val EXTENSION_CLASS =
    "Lapp/revanced/extension/gamehub/ui/GameIdHelper;"

private val gameIdLayoutPatch = resourcePatch {
    execute {
        // Register resource IDs so getIdentifier() can resolve them at runtime
        document("res/values/ids.xml").use { dom ->
            val root = dom.documentElement
            val existingIds = dom.getElementsByTagName("item").asSequence()
                .map { (it as Element).getAttribute("name") }
                .toSet()

            val newIds = listOf(
                "tv_steam_app_id",
                "tv_local_game_id",
                "ll_game_id_container",
            )

            for (id in newIds) {
                if (id !in existingIds) {
                    dom.createElement("item").apply {
                        setAttribute("name", id)
                        setAttribute("type", "id")
                    }.let(root::appendChild)
                }
            }
        }

        // Inject game ID container into the game detail layout
        try {
            document("res/layout/llauncher_activity_gamedetail.xml").use { dom ->
                val root = dom.documentElement

                // Check if already present
                val existing = dom.getElementsByTagName("*").asSequence()
                    .any { (it as Element).getAttribute("android:id")?.contains("ll_game_id_container") == true }
                if (existing) return@execute

                // Vertical container with two tap-to-copy TextViews
                val container = dom.createElement("LinearLayout").apply {
                    setAttribute("android:id", "@id/ll_game_id_container")
                    setAttribute("android:orientation", "vertical")
                    setAttribute("android:layout_width", "wrap_content")
                    setAttribute("android:layout_height", "wrap_content")
                    setAttribute("android:paddingTop", "@dimen/mw_8dp")
                    setAttribute("android:paddingStart", "@dimen/mw_60dp")
                    setAttribute("android:visibility", "gone")
                    setAttribute("app:layout_constraintStart_toStartOf", "parent")
                    setAttribute("app:layout_constraintTop_toBottomOf", "@id/topBarView")

                    // Steam App ID (tap to copy)
                    dom.createElement("TextView").apply {
                        setAttribute("android:id", "@id/tv_steam_app_id")
                        setAttribute("android:layout_width", "wrap_content")
                        setAttribute("android:layout_height", "wrap_content")
                        setAttribute("android:textSize", "@dimen/mw_12sp")
                        setAttribute("android:textColor", "#ffffffff")
                        setAttribute("android:visibility", "gone")
                        setAttribute("android:focusable", "true")
                        setAttribute("android:clickable", "true")
                    }.let(this::appendChild)

                    // Local Game ID (tap to copy)
                    dom.createElement("TextView").apply {
                        setAttribute("android:id", "@id/tv_local_game_id")
                        setAttribute("android:layout_width", "wrap_content")
                        setAttribute("android:layout_height", "wrap_content")
                        setAttribute("android:textSize", "@dimen/mw_12sp")
                        setAttribute("android:textColor", "#ffffffff")
                        setAttribute("android:visibility", "gone")
                        setAttribute("android:focusable", "true")
                        setAttribute("android:clickable", "true")
                    }.let(this::appendChild)
                }

                // Insert before the include_skeleton
                val includeNodes = dom.getElementsByTagName("include").asSequence().toList()
                val skeleton = includeNodes.firstOrNull {
                    (it as Element).getAttribute("android:id")?.contains("include_skeleton") == true
                }
                if (skeleton != null) {
                    root.insertBefore(container, skeleton)
                } else {
                    root.appendChild(container)
                }
            }
        } catch (_: Exception) {}
    }
}

@Suppress("unused")
val gameIdDisplayPatch = bytecodePatch(
    name = "Game ID display",
    description = "Displays a copyable game ID in the game detail screen.",
) {
    compatibleWith("com.xiaoji.egggame"("5.3.5"))
    dependsOn(sharedGamehubExtensionPatch, gameIdLayoutPatch)

    execute {
        // The return-void in onCreate has the :cond_0 label on it (the logged-in branch jumps there).
        // Using replaceInstruction moves the label to our call, then we append a new return-void.
        gameDetailOnCreateFingerprint.method.apply {
            val returnIndex = implementation!!.instructions.size - 1
            replaceInstruction(returnIndex, "invoke-static {p0}, $EXTENSION_CLASS->populateGameId(Landroid/app/Activity;)V")
            addInstruction(returnIndex + 1, "return-void")
        }
    }
}
