package app.revanced.patches.all.misc.play

import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.removeInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.intOption
import app.revanced.patcher.patch.option
import app.revanced.util.forEachInstructionAsSequence
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.util.MethodUtil

@Suppress("unused")
val spoofAgeSignalsPatch = bytecodePatch(
    name = "Spoof Age Signals",
    description = "Spoofs information about the user age.",
    use = false,
) {
    val ageLower by intOption(
        name = "Lower age bound",
        description = "A positive integer.",
        default = 18,
        validator = { it == null || it > 0 },
    )

    val ageUpper by intOption(
        name = "Lower age bound",
        description = "A positive integer. Must be greater than the lower age bound.",
        default = Int.MAX_VALUE,
        validator = { it == null || it > ageLower!! },
    )

    apply {
        forEachInstructionAsSequence(
            match = { _, _, instruction, instructionIndex ->
                if (instruction !is ReferenceInstruction) return@forEachInstructionAsSequence null

                val reference = instruction.reference as? MethodReference ?: return@forEachInstructionAsSequence null

                val match = MethodCall.entries.firstOrNull { search ->
                    MethodUtil.methodSignaturesMatch(reference, search.reference)
                } ?: return@forEachInstructionAsSequence null

                val replacement = when (match) {
                    MethodCall.AgeLower -> ageLower!!
                    MethodCall.AgeUpper -> ageUpper!!
                }

                replacement.let { instructionIndex to it }
            },
            transform = ::transformMethodCall
        )
    }
}

private fun transformMethodCall(
    mutableMethod: MutableMethod,
    entry: Pair<Int, Int>,
) {
    val (instructionIndex, replacement) = entry

    // Get the register which would have contained the return value
    val register = mutableMethod.getInstruction<OneRegisterInstruction>(instructionIndex + 1).registerA

    // Replace the call instructions with our fake value
    mutableMethod.removeInstructions(instructionIndex, 2)
    mutableMethod.addInstructions(
        instructionIndex,
        """
            const v$register, $replacement
            invoke-static { v$register }, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
            move-result-object v$register
        """.trimIndent(),
    )
}

private enum class MethodCall(
    val reference: MethodReference,
) {
    AgeLower(
        ImmutableMethodReference(
            "Lcom/google/android/play/agesignals/AgeSignalsResult;",
            "ageLower",
            emptyList(),
            "Ljava/lang/Integer;",
        ),
    ),
    AgeUpper(
        ImmutableMethodReference(
            "Lcom/google/android/play/agesignals/AgeSignalsResult;",
            "ageUpper",
            emptyList(),
            "Ljava/lang/Integer;",
        ),
    ),
}

