package app.revanced.patches.youtube.misc.recyclerviewtree.hook.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.recyclerviewtree.hook.fingerprints.RecyclerViewTreeObserverFingerprint

@DependsOn([IntegrationsPatch::class])
class RecyclerViewTreeHookPatch : BytecodePatch(
    listOf(RecyclerViewTreeObserverFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        RecyclerViewTreeObserverFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.startIndex + 5
                val recyclerViewParameter = 2

                addHook = { classDescriptor ->
                    addInstruction(
                        insertIndex,
                        "invoke-static/range { p$recyclerViewParameter .. p$recyclerViewParameter }, $classDescriptor->onFlyoutMenuCreate(Landroid/support/v7/widget/RecyclerView;)V"
                    )
                }
            }
        } ?: throw RecyclerViewTreeObserverFingerprint.exception

    }

    internal companion object {
        internal lateinit var addHook: (String) -> Unit
            private set
    }
}