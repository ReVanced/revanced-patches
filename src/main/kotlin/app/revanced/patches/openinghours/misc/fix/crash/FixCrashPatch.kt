package app.revanced.patches.openinghours.misc.fix.crash

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.extensions.newLabel
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.openinghours.misc.fix.crash.fingerprints.SetPlaceFingerprint
import app.revanced.util.exception
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21t
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Fix crash",
    compatiblePackages = [CompatiblePackage("de.simon.openinghours", ["1.0"])],
)
@Suppress("unused")
object FixCrashPatch : BytecodePatch(
    setOf(SetPlaceFingerprint),
) {

    override fun execute(context: BytecodeContext) {
        SetPlaceFingerprint.result?.let {
            val indexedInstructions = it.mutableMethod.getInstructions().withIndex().toList()

            /**
             * This function replaces all `checkNotNull` instructions in the integer interval
             * from [startIndex] to [endIndex], both inclusive. In place of the `checkNotNull`
             * instruction an if-null check is inserted. If the if-null check yields that
             * the value is indeed null, we jump to a newly created label at `endIndex + 1`.
             */
            fun avoidNullPointerException(startIndex: Int, endIndex: Int) {
                val continueLabel = it.mutableMethod.newLabel(endIndex + 1)

                for (index in startIndex..endIndex) {
                    val instruction = indexedInstructions[index].value

                    if (!instruction.isCheckNotNullInstruction) {
                        continue
                    }

                    val checkNotNullInstruction = instruction as FiveRegisterInstruction
                    val originalRegister = checkNotNullInstruction.registerC

                    it.mutableMethod.replaceInstruction(
                        index,
                        BuilderInstruction21t(
                            Opcode.IF_EQZ,
                            originalRegister,
                            continueLabel,
                        ),
                    )
                }
            }

            val getOpeningHoursIndex = getIndicesOfInvoke(
                indexedInstructions,
                "Lde/simon/openinghours/models/Place;",
                "getOpeningHours",
            )

            val setWeekDayTextIndex = getIndexOfInvoke(
                indexedInstructions,
                "Lde/simon/openinghours/views/custom/PlaceCard;",
                "setWeekDayText",
            )

            val startCalculateStatusIndex = getIndexOfInvoke(
                indexedInstructions,
                "Lde/simon/openinghours/views/custom/PlaceCard;",
                "startCalculateStatus",
            )

            // Replace the Intrinsics;->checkNotNull instructions with a null check
            // and jump to our newly created label if it returns true.
            // This avoids the NullPointerExceptions.
            avoidNullPointerException(getOpeningHoursIndex[1], startCalculateStatusIndex)
            avoidNullPointerException(getOpeningHoursIndex[0], setWeekDayTextIndex)
        } ?: throw SetPlaceFingerprint.exception
    }

    private fun isInvokeInstruction(instruction: Instruction, className: String, methodName: String): Boolean {
        val methodRef = instruction.getReference<MethodReference>() ?: return false
        return methodRef.definingClass == className && methodRef.name == methodName
    }

    private fun getIndicesOfInvoke(
        instructions: List<IndexedValue<Instruction>>,
        className: String,
        methodName: String,
    ): List<Int> = instructions.mapNotNull { (index, instruction) ->
        if (isInvokeInstruction(instruction, className, methodName)) {
            index
        } else {
            null
        }
    }

    private fun getIndexOfInvoke(
        instructions: List<IndexedValue<Instruction>>,
        className: String,
        methodName: String,
    ): Int = instructions.first { (_, instruction) ->
        isInvokeInstruction(instruction, className, methodName)
    }.index

    private val Instruction.isCheckNotNullInstruction
        get() = isInvokeInstruction(this, "Lkotlin/jvm/internal/Intrinsics;", "checkNotNull")
}
