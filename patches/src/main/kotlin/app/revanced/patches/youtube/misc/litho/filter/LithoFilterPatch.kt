@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.youtube.misc.litho.filter

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.playservice.is_19_17_or_greater
import app.revanced.patches.youtube.misc.playservice.is_19_25_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_05_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.shared.conversionContextFingerprintToString
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Deprecated("Use the shared one instead", ReplaceWith("app.revanced.patches.shared.misc.litho.filter.addLithoFilter"))
lateinit var addLithoFilter: (String) -> Unit
    private set

val lithoFilterPatch = lithoFilterPatch(
    componentCreateInsertionIndex = {
        if (is_19_17_or_greater) {
            indexOfFirstInstructionOrThrow(Opcode.RETURN_OBJECT)
        } else {
            // 19.16 clobbers p2 so must check at start of the method
            0
        }
    },
    conversionContextFingerprintToString = conversionContextFingerprintToString,
    executeBlock = BytecodePatchContext::executeBlock,
) {
    dependsOn(versionCheckPatch)
}

private fun BytecodePatchContext.executeBlock() {
    // region A/B test of new Litho native code.

    // Turn off native code that handles litho component names.  If this feature is on then nearly
    // all litho components have a null name and identifier/path filtering is completely broken.
    //
    // Flag was removed in 20.05. It appears a new flag might be used instead (45660109L),
    // but if the flag is forced on then litho filtering still works correctly.
    if (is_19_25_or_greater && !is_20_05_or_greater) {
        lithoComponentNameUpbFeatureFlagFingerprint.method.apply {
            // Don't use return early, so the debug patch logs if this was originally on.
            val insertIndex = indexOfFirstInstructionOrThrow(Opcode.RETURN)
            val register = getInstruction<OneRegisterInstruction>(insertIndex).registerA

            addInstruction(insertIndex, "const/4 v$register, 0x0")
        }
    }

    // Turn off a feature flag that enables native code of protobuf parsing (Upb protobuf).
    // If this is enabled, then the litho protobuffer hook will always show an empty buffer
    // since it's no longer handled by the hooked Java code.
    lithoConverterBufferUpbFeatureFlagFingerprint.method.apply {
        val index = indexOfFirstInstructionOrThrow(Opcode.MOVE_RESULT)
        val register = getInstruction<OneRegisterInstruction>(index).registerA

        addInstruction(index + 1, "const/4 v$register, 0x0")
    }

    // endregion

    // Set the addLithoFilter function to the one from the shared patch.
    // This is done for backwards compatibility.
    addLithoFilter = app.revanced.patches.shared.misc.litho.filter.addLithoFilter
}
