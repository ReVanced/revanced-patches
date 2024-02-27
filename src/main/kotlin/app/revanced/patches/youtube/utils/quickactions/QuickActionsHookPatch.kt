package app.revanced.patches.youtube.utils.quickactions

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.utils.integrations.Constants.FULLSCREEN
import app.revanced.patches.youtube.utils.quickactions.fingerprints.QuickActionsElementFingerprint
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.QuickActionsElementContainer
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.WideLiteralInstruction

@Patch(dependencies = [SharedResourceIdPatch::class])
object QuickActionsHookPatch : BytecodePatch(
    setOf(QuickActionsElementFingerprint)
) {
    private lateinit var insertMethod: MutableMethod
    private var insertIndex: Int = 0
    private var insertRegister: Int = 0
    override fun execute(context: BytecodeContext) {

        QuickActionsElementFingerprint.result?.let {
            it.mutableMethod.apply {
                insertMethod = this
                for (index in implementation!!.instructions.size - 1 downTo 0) {
                    if (getInstruction(index).opcode == Opcode.CONST && (getInstruction(index) as WideLiteralInstruction).wideLiteral == QuickActionsElementContainer) {
                        insertIndex = index + 3
                        insertRegister =
                            getInstruction<OneRegisterInstruction>(index + 2).registerA

                        addInstruction(
                            insertIndex,
                            "invoke-static {v$insertRegister}, $FULLSCREEN->hideQuickActions(Landroid/view/View;)V"
                        )
                        insertIndex += 2

                        break
                    }
                }
            }
        } ?: throw QuickActionsElementFingerprint.exception
    }

    internal fun injectQuickActionMargin() {
        insertMethod.apply {
            addInstruction(
                insertIndex,
                "invoke-static {v$insertRegister}, $FULLSCREEN->setQuickActionMargin(Landroid/widget/FrameLayout;)V"
            )
        }
    }
}
