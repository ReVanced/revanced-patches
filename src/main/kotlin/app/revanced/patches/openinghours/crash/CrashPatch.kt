package app.revanced.patches.openinghours.crash

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.extensions.newLabel
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.openinghours.crash.fingerprints.SetPlaceFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.BuilderInstruction
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21t
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Fix crash",
    compatiblePackages = [CompatiblePackage("de.simon.openinghours", ["1.0"])]
)
@Suppress("unused")
object CrashPatch : BytecodePatch(setOf(SetPlaceFingerprint)) {

    override fun execute(context: BytecodeContext) {
        with(SetPlaceFingerprint.result ?: throw SetPlaceFingerprint.exception) {
            val indexedInstructions = mutableMethod.implementation!!.instructions.withIndex().toList()
            val getOpeningHoursIndex = getIndicesOfInvoke(indexedInstructions, "Lde/simon/openinghours/models/Place;", "getOpeningHours")
            val setWeekDayTextIndex = getIndicesOfInvoke(indexedInstructions, "Lde/simon/openinghours/views/custom/PlaceCard;", "setWeekDayText")[0]
            val startCalculateStatusIndex = getIndicesOfInvoke(indexedInstructions, "Lde/simon/openinghours/views/custom/PlaceCard;", "startCalculateStatus")[0]
            val getOpeningHoursIndex1 = getOpeningHoursIndex[0]
            val getOpeningHoursIndex2 = getOpeningHoursIndex[1]

            // We replace the Intrinsics;->checkNotNull instructions with a simple
            // null check and jump to our newly created label if it returns true.
            // Thus, we avoid NullPointerExceptions.
            val continueLabel2 = mutableMethod.newLabel(startCalculateStatusIndex + 1)

            for (index in getOpeningHoursIndex2..startCalculateStatusIndex) {
                val instruction = indexedInstructions[index].value

                if (!isCheckNotNullInstruction(instruction)) {
                    continue
                }

                mutableMethod.replaceInstruction(index, BuilderInstruction21t(Opcode.IF_EQZ, 4, continueLabel2))
            }

            // Same here, we replace the Intrinsics;->checkNotNull instructions.
            val continueLabel1 = mutableMethod.newLabel(setWeekDayTextIndex + 1)

            for (index in getOpeningHoursIndex1..setWeekDayTextIndex) {
                val instruction = indexedInstructions[index].value

                if (!isCheckNotNullInstruction(instruction)) {
                    continue
                }

                mutableMethod.replaceInstruction(index, BuilderInstruction21t(Opcode.IF_EQZ, 0, continueLabel1))
            }
        }
    }

    private fun isInvokeInstruction(instruction: BuilderInstruction, className: String, methodName: String): Boolean {
        val invokeInstruction = instruction as? ReferenceInstruction ?: return false
        val methodRef = invokeInstruction.reference as? MethodReference ?: return false
        return methodRef.definingClass == className && methodRef.name == methodName
    }

    private fun getIndicesOfInvoke(
        instructions: List<IndexedValue<BuilderInstruction>>,
        className: String,
        methodName: String,
    ): List<Int> = instructions.mapNotNull { (index, instruction) ->
        if (isInvokeInstruction(instruction, className, methodName)) {
            index
        } else {
            null
        }
    }

    private fun isCheckNotNullInstruction(instruction: BuilderInstruction) =
        isInvokeInstruction(instruction, "Lkotlin/jvm/internal/Intrinsics;", "checkNotNull")

}
