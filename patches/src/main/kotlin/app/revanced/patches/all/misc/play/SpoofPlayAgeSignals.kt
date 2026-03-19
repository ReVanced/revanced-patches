package app.revanced.patches.all.misc.play

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.removeInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.intOption
import app.revanced.patcher.patch.option
import app.revanced.util.forEachInstructionAsSequence
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference

@Suppress("unused")
val spoofPlayAgeSignalsPatch = bytecodePatch(
    name = "Spoof Play Age Signals",
    description = "Spoofs Google Play data about the user's age and verification status.",
    use = false,
) {
    val lowerAgeBound by intOption(
        name = "Lower age bound",
        description = "A positive integer.",
        default = 18,
        validator = { it == null || it > 0 },
    )

    val upperAgeBound by intOption(
        name = "Upper age bound",
        description = "A positive integer. Must be greater than the lower age bound.",
        default = Int.MAX_VALUE,
        validator = { it == null || it > lowerAgeBound!! },
    )

    val userStatus by intOption(
        name = "User status",
        description = "An integer representing the user status.",
        default = UserStatus.VERIFIED.value,
        values = UserStatus.entries.associate { it.name to it.value },
    )

    apply {
        forEachInstructionAsSequence(match = { classDef, _, instruction, instructionIndex ->
            // Avoid patching the library itself.
            if (classDef.type.startsWith("Lcom/google/android/play/agesignals/")) return@forEachInstructionAsSequence null

            // Keep method calls only.
            val reference = instruction.getReference<MethodReference>()
                ?: return@forEachInstructionAsSequence null

            val match = MethodCall.entries.firstOrNull {
                reference == it.reference
            } ?: return@forEachInstructionAsSequence null

            val replacement = when (match) {
                MethodCall.AgeLower -> lowerAgeBound!!
                MethodCall.AgeUpper -> upperAgeBound!!
                MethodCall.UserStatus -> userStatus!!
            }

            replacement.let { instructionIndex to it }
        }, transform = { method, entry ->
            val (instructionIndex, replacement) = entry

            // Get the register which would have contained the return value.
            val register = method.getInstruction<OneRegisterInstruction>(instructionIndex + 1).registerA

            // Replace the call instructions with the spoofed value.
            method.removeInstructions(instructionIndex, 2)
            method.addInstructions(
                instructionIndex,
                """
                    const v$register, $replacement
                    invoke-static { v$register }, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                    move-result-object v$register
                """.trimIndent(),
            )
        })
    }
}

/**
 * See [AgeSignalsResult](https://developer.android.com/google/play/age-signals/reference/com/google/android/play/agesignals/AgeSignalsResult).
 */
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
    UserStatus(
        ImmutableMethodReference(
            "Lcom/google/android/play/agesignals/AgeSignalsResult;",
            "userStatus",
            emptyList(),
            "Ljava/lang/Integer;",
        ),
    ),
}

/**
 * All possible user verification statuses.
 *
 * See [AgeSignalsVerificationStatus](https://developer.android.com/google/play/age-signals/reference/com/google/android/play/agesignals/model/AgeSignalsVerificationStatus).
 */
private enum class UserStatus(val value: Int) {
    /** The user provided their age, but it hasn't been verified yet. */
    DECLARED(5),

    /** The user is 18+. */
    VERIFIED(0),

    /** The user's guardian has set the age for him. */
    SUPERVISED(1),

    /** The user's guardian hasn't approved the significant changes yet. */
    SUPERVISED_APPROVAL_PENDING(2),

    /** The user's guardian has denied approval for one or more pending significant changes. */
    SUPERVISED_APPROVAL_DENIED(3),

    /** The user is not verified or supervised. */
    UNKNOWN(4),
}
