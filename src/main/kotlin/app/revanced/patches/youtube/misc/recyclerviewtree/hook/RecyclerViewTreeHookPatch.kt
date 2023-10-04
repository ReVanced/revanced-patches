package app.revanced.patches.youtube.misc.recyclerviewtree.hook

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.recyclerviewtree.hook.fingerprints.RecyclerViewTreeObserverFingerprint

@Patch(
    dependencies = [IntegrationsPatch::class]
)
object RecyclerViewTreeHookPatch : BytecodePatch(
    setOf(RecyclerViewTreeObserverFingerprint)
) {
    internal lateinit var addHook: (String) -> Unit
        private set

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
}