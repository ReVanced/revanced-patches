package app.revanced.patches.youtube.utils.browseid

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.patch.litho.ComponentParserPatch
import app.revanced.patches.youtube.utils.browseid.fingerprints.BrowseIdClassFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.UTILS_PATH
import app.revanced.patches.youtube.utils.litho.LithoFilterPatch
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.util.exception
import app.revanced.util.getStringInstructionIndex
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Patch(
    dependencies = [
        LithoFilterPatch::class,
        SharedResourceIdPatch::class
    ]
)
object BrowseIdHookPatch : BytecodePatch(
    setOf(BrowseIdClassFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "$UTILS_PATH/BrowseIdPatch;"

    override fun execute(context: BytecodeContext) {

        /**
         * This class handles BrowseId.
         * Pass an instance of this class to integrations to use Java Reflection.
         */
        BrowseIdClassFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = getStringInstructionIndex("VL") - 1
                val targetReference = getInstruction<ReferenceInstruction>(targetIndex).reference
                val targetClass = context.findClass((targetReference as FieldReference).definingClass)!!.mutableClass

                targetClass.methods.find { method -> method.name == "<init>" }
                    ?.apply {
                        val browseIdFieldIndex = implementation!!.instructions.indexOfFirst { instruction ->
                            instruction.opcode == Opcode.IPUT_OBJECT
                        }
                        val browseIdFieldName =
                            (getInstruction<ReferenceInstruction>(browseIdFieldIndex).reference as FieldReference).name

                        addInstructions(
                            1, """
                                const-string v0, "$browseIdFieldName"
                                invoke-static {p0, v0}, $INTEGRATIONS_CLASS_DESCRIPTOR->initialize(Ljava/lang/Object;Ljava/lang/String;)V
                                """
                        )
                    } ?: throw PatchException("BrowseIdClass not found!")
            }
        } ?: throw BrowseIdClassFingerprint.exception

        /**
         * Set BrowseId to integrations.
         */
        ComponentParserPatch.insertMethod.apply {
            addInstruction(
                0,
                "invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->setBrowseIdFromField()V"
            )
        }
    }
}