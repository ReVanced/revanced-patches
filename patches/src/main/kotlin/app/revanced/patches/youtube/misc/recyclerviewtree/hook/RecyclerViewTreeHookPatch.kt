package app.revanced.patches.youtube.misc.recyclerviewtree.hook

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch

lateinit var addRecyclerViewTreeHook: (String) -> Unit
    private set

val recyclerViewTreeHookPatch = bytecodePatch {
    dependsOn(sharedExtensionPatch)

    apply {
        recyclerViewTreeObserverMethodMatch.let {
            val insertIndex = it.indices.first() + 1
            val recyclerViewParameter = 2

            addRecyclerViewTreeHook = { classDescriptor ->
                it.method.addInstruction(
                    insertIndex,
                    "invoke-static/range { p$recyclerViewParameter .. p$recyclerViewParameter }, " +
                        "$classDescriptor->onFlyoutMenuCreate(Landroid/support/v7/widget/RecyclerView;)V",
                )
            }
        }
    }
}
