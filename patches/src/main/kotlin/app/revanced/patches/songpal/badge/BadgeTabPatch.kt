package app.revanced.patches.songpal.badge

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.bytecodePatch

internal const val ACTIVITY_TAB_DESCRIPTOR = "Ljp/co/sony/vim/framework/ui/yourheadphones/YhContract\$Tab;"

@Suppress("unused")
val badgeTabPatch = bytecodePatch(
    name = "Remove badge tab",
    description = "Removes the badge tab from the activity tab.",
) {
    compatibleWith("com.sony.songpal.mdr")

    val arrayTabs = listOf("Log", "HealthCare")

    execute {
        createTabsFingerprint.method.apply {
            removeInstructions(0, 2)

            val arrayRegister = 0
            val indexRegister = 1
            val arrayItemRegister = 2

            // First insert the array of tabs...

            arrayTabs.withIndex().forEach { (index, tab) ->
                addInstructions(
                    0,
                    """
                        const/4 v$indexRegister, $index
                        sget-object v$arrayItemRegister, $ACTIVITY_TAB_DESCRIPTOR->$tab:$ACTIVITY_TAB_DESCRIPTOR
                        aput-object v$arrayItemRegister, v$arrayRegister, v$indexRegister
                    """,
                )
            }

            // Then add the instructions to initialize the array.
            // This is done so that the order of instructions is correct.

            addInstructions(
                0,
                """
                    const/4 v$arrayRegister, ${arrayTabs.size}
                    new-array v$arrayRegister, v$arrayRegister, [$ACTIVITY_TAB_DESCRIPTOR
                 """,
            )
        }
    }
}
