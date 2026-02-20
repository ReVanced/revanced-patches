package app.revanced.patches.gamehub.ui.statusbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.gamehub.misc.extension.sharedGamehubExtensionPatch
import app.revanced.util.asSequence
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import org.w3c.dom.Element

private const val EXTENSION_CLASS =
    "Lapp/revanced/extension/gamehub/ui/BatteryHelper;"

private val batteryLayoutPatch = resourcePatch {
    execute {
        // Add resource IDs to ids.xml (idempotent)
        document("res/values/ids.xml").use { dom ->
            val root = dom.documentElement
            val existingIds = dom.getElementsByTagName("item").asSequence()
                .map { (it as Element).getAttribute("name") }
                .toSet()

            val newIds = listOf(
                "tv_battery_percent",
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

        // Modify layouts to add battery percentage and hide online indicator
        val layouts = listOf(
            "res/layout/comm_view_top_bar.xml",
            "res/layout/llauncher_activity_launcher_main.xml",
            "res/layout/llauncher_activity_new_launcher_main.xml",
            "res/layout/llauncher_v4_search_activity.xml",
        )

        for (layoutPath in layouts) {
            try {
                document(layoutPath).use { dom ->
                    val allElements = dom.getElementsByTagName("*")

                    // Find battery icon ImageView and insert sibling TextView after it
                    for (i in 0 until allElements.length) {
                        val el = allElements.item(i) as? Element ?: continue
                        val id = el.getAttribute("android:id") ?: ""
                        if (id.contains("battery") && el.tagName == "ImageView") {
                            val parent = el.parentNode ?: continue
                            val batteryText = dom.createElement("TextView").apply {
                                setAttribute("android:id", "@+id/tv_battery_percent")
                                setAttribute("android:layout_width", "wrap_content")
                                setAttribute("android:layout_height", "wrap_content")
                                setAttribute("android:text", "")
                                setAttribute("android:textSize", "12sp")
                                setAttribute("android:textColor", "#ffffffff")
                                setAttribute("android:textStyle", "bold")
                                setAttribute("android:layout_marginStart", "4dp")
                                setAttribute("android:gravity", "center_vertical")
                            }
                            // Insert after the battery ImageView
                            val nextSibling = el.nextSibling
                            if (nextSibling != null) {
                                parent.insertBefore(batteryText, nextSibling)
                            } else {
                                parent.appendChild(batteryText)
                            }
                            break
                        }
                    }

                }
            } catch (_: Exception) {}
        }
    }
}

@Suppress("unused")
val batteryDisplayPatch = bytecodePatch(
    name = "Battery percentage display",
    description = "Adds a battery percentage text next to the battery icon.",
) {
    compatibleWith("com.xiaoji.egggame"("5.3.5"))
    dependsOn(sharedGamehubExtensionPatch, batteryLayoutPatch)

    execute {
        // BatteryUtil.a(Context, ImageView): after getIntProperty returns battery level
        // in p0, inject a call to update the battery percentage TextView.
        // At the injection point: p0 = battery level (int), p2 = ImageView.
        batteryUtilFingerprint.method.apply {
            val moveResultIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.MOVE_RESULT && this is com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
            }

            // Insert after move-result p0
            addInstructions(
                moveResultIndex + 1,
                """
                    invoke-static {p2, p0}, $EXTENSION_CLASS->updateBatteryText(Landroid/widget/ImageView;I)V
                """,
            )
        }
    }
}
