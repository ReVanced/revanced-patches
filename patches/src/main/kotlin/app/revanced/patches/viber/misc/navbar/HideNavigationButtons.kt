package app.revanced.patches.viber.misc.navbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.booleanOption
import app.revanced.patcher.patch.bytecodePatch
import java.util.logging.Logger

@Suppress("unused")
val hideNavigationButtonsPatch = bytecodePatch(
    name = "Hide navigation buttons",
    description = "Hides selected navigation bar buttons, e.g. Explore and Marketplace.",
) {
    compatibleWith("com.viber.voip"("26.1.2.0"))

    val hideOptions = AllowedNavigationItems.entries.associateWith {
        booleanOption(
            key = it.key,
            default = it.defaultHideOption,
            title = it.title,
            description = it.description,
        )
    }

    execute {
        // Items that won't be forcefully hidden.
        val allowedItems = hideOptions.filter { (option, enabled) -> enabled.value != true }

        if (allowedItems.isEmpty()) {
            return@execute Logger.getLogger(this::class.java.name).warning(
                "No hide navigation buttons options are enabled. No changes made."
            )
        }

        val tabClass = tabIdClassFingerprint.classDef
        fingerprint {
            parameters("I", "I")
            returns("Z")
            custom { methodDef, classDef ->
                classDef == tabClass
            }
        }
        .method
        .apply{
            // Build the injection instructions
            val instructions =
                AllowedNavigationItems.buildInjectionInstructions(allowedItems.map { it.key })

            addInstructionsWithLabels(0, instructions)
        }
    }
}

/**
 * Navigation items taken from source code.
 * They appear in code like new NavigationItem(0, R.string.bottom_tab_chats, R.drawable.ic_tab_chats)
 */
private enum class AllowedNavigationItems(
    val defaultHideOption: Boolean,
    private val itemName: String,
    private vararg val ids: Int
) {
    CHATS(false, "Chats", 0),
    CALLS(false, "Calls", 1, 7),
    EXPLORE(true, "Explore", 2),
    MORE(false, "More", 3),
    PAY(true, "Pay", 5),
    CAMERA(true, "Camera", 6),
    MARKETPLACE(true, "Marketplace", 8);

    val key = "hide$itemName"
    val title = "Hide $itemName"
    val description = "Permanently hides the $itemName button."

    fun buildAllowInstruction(): String =
        ids.joinToString("\n") { id ->
            """
            const/4 v0, $id  # If tabId == $id ($itemName), don't hide it
            if-eq p1, v0, :continue
            """
        }


    companion object {
        fun buildInjectionInstructions(allowedItems: List<AllowedNavigationItems>): String {
            """
                # If we reach this, it means that this tab has been disabled by user
                const/4 v0, 0
                return v0  # return false as "This tab is not enabled"
                       
                # Proceed with default execution
                :continue
                nop
            """.let {
                return allowedItems
                    .joinToString("\n") { it.buildAllowInstruction() } + "\n" + it
            }
        }
    }
}
