package app.revanced.patches.viber.misc.navbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.booleanOption
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.PATCH_NAME_HIDE_NAVIGATION_BUTTONS
import java.util.logging.Logger
import kotlin.collections.joinToString

@Suppress("unused")
val hideNavigationButtonsPatch = bytecodePatch(
    name = PATCH_NAME_HIDE_NAVIGATION_BUTTONS,
    description = "Permanently hides navigation bar buttons, such as Explore and Marketplace.",
    use = false
) {
    compatibleWith("com.viber.voip")

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

        if (allowedItems.size == AllowedNavigationItems.entries.size) {
            return@execute Logger.getLogger(this::class.java.name).warning(
                "No hide navigation buttons options are enabled. No changes applied."
            )
        }

        val injectionInstructions = allowedItems
            .map { it.key.buildAllowInstruction() }
            .joinToString("\n") + """
                # If we reach this, it means that this tab has been disabled by user
                const/4 v0, 0
                return v0  # return false as "This tab is not enabled"
                       
                # Proceed with default execution
                :continue
                nop
            """

        shouldShowTabIdMethodFingerprint
            .method
            .addInstructionsWithLabels(0, injectionInstructions)
    }
}

/**
 * Navigation items taken from source code.
 * They appear in code like new NavigationItem(0, R.string.bottom_tab_chats, R.drawable.ic_tab_chats).
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
}
