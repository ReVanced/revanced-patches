package app.revanced.patches.shared.patch.litho

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.fingerprints.litho.LithoThemeFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

object LithoThemePatch : BytecodePatch(
    setOf(LithoThemeFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        LithoThemeFingerprint.result?.mutableMethod?.let {
            with(it.implementation!!.instructions) {
                for (index in size - 1 downTo 0) {
                    val invokeInstruction = this[index] as? ReferenceInstruction ?: continue
                    if ((invokeInstruction.reference as MethodReference).name != "setColor") continue
                    insertIndex = index
                    insertRegister = (this[index] as Instruction35c).registerD
                    insertMethod = it
                    break
                }
            }
        } ?: throw LithoThemeFingerprint.exception

    }

    private var offset = 0

    private var insertIndex: Int = 0
    private var insertRegister: Int = 0
    private lateinit var insertMethod: MutableMethod


    fun injectCall(
        methodDescriptor: String
    ) {
        insertMethod.addInstructions(
            insertIndex + offset, """
                    invoke-static {v$insertRegister}, $methodDescriptor
                    move-result v$insertRegister
                    """
        )
        offset += 2
    }
}

