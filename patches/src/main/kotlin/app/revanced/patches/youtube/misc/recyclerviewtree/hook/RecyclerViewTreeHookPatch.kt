package app.revanced.patches.youtube.misc.recyclerviewtree.hook

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch

lateinit var addRecyclerViewTreeHook: (String) -> Unit
    private set

val recyclerViewTreeHookPatch = bytecodePatch {
    dependsOn(sharedExtensionPatch)

    execute {
        recyclerViewTreeObserverFingerprint.method.apply {
            val insertIndex = recyclerViewTreeObserverFingerprint.instructionMatches.first().index + 1
            val recyclerViewParameter = 2

            addRecyclerViewTreeHook = { classDescriptor ->
                addInstruction(
                    insertIndex,
                    "invoke-static/range { p$recyclerViewParameter .. p$recyclerViewParameter }, " +
                        "$classDescriptor->onFlyoutMenuCreate(Landroid/support/v7/widget/RecyclerView;)V",
                )
            }
        }
    }
}
