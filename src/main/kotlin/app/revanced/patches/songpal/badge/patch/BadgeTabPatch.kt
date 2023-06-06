package app.revanced.patches.songpal.badge.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.songpal.badge.annotations.BadgeCompatibility
import app.revanced.patches.songpal.badge.fingerprints.CreateTabsFingerprint

@Patch
@Name("remove-badge-tab")
@Description("Removes the badge tab from the activity tab.")
@BadgeCompatibility
@Version("0.0.1")
class BadgeTabPatch : BytecodePatch(
    listOf(CreateTabsFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        CreateTabsFingerprint.result?.mutableMethod?.apply {
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
                    """
                )
            }

            // Then add the instructions to initialize the array.
            // This is done so that the order of instructions is correct.

            addInstructions(
                0,
                """
                    const/4 v$arrayRegister, ${arrayTabs.size}
                    new-array v$arrayRegister, v$arrayRegister, [$ACTIVITY_TAB_DESCRIPTOR
                 """
            )

        } ?: return CreateTabsFingerprint.toErrorResult()

        return PatchResultSuccess()
    }

    companion object {
        const val ACTIVITY_TAB_DESCRIPTOR = "Ljp/co/sony/vim/framework/ui/yourheadphones/YhContract\$Tab;"
        val arrayTabs = listOf("Log", "HealthCare")
    }
}
