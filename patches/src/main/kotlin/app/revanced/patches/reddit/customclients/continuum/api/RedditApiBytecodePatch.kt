package app.revanced.patches.reddit.customclients.continuum.api

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.transformation.transformInstructionsPatch
import app.revanced.patches.reddit.customclients.continuum.misc.removeClientIdCheckPatch
import app.revanced.patches.shared.misc.string.replaceStringPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Suppress("unused")
val redditApiBytecodePatch = bytecodePatch(
    name = "Reddit API override",
    description = "Overrides Reddit User-Agent, Redirect URI, and Client ID",
) {
    compatibleWith("org.cygnusx1.continuum", "org.cygnusx1.continuum.debug")

    dependsOn(
        redditApiResourcePatch,
        // Use regex-based replacement for user agent to work across all versions
        transformInstructionsPatch(
            filterMap = filterMap@{ _, _, instruction, instructionIndex ->
                if (instruction.opcode != Opcode.CONST_STRING && instruction.opcode != Opcode.CONST_STRING_JUMBO) {
                    return@filterMap null
                }

                val stringReference = (instruction as? com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction)
                    ?.reference as? StringReference ?: return@filterMap null

                val pattern = "android:org\\.cygnusx1\\.continuum:\\d+\\.\\d+\\.\\d+\\.\\d+ \\(by /u/edgan\\)".toRegex()
                if (!pattern.matches(stringReference.string)) return@filterMap null

                Triple(instructionIndex, instruction as OneRegisterInstruction, stringReference.string)
            },
            transform = { mutableMethod, entry ->
                val (instructionIndex, instruction, _) = entry
                mutableMethod.replaceInstruction(
                    instructionIndex,
                    "${instruction.opcode.name} v${instruction.registerA}, \"${Constants.NEW_USER_AGENT}\"",
                )
            },
        ),
        replaceStringPatch(Constants.OLD_REDIRECT_URI, Constants.NEW_REDIRECT_URI),
        removeClientIdCheckPatch
    )

    execute {
        // The actual replacements are handled by the dependencies:
        // - redditApiResourcePatch: modifies default_client_id in res/values/strings.xml
        // - transformInstructionsPatch: replaces user agent with regex pattern matching
        // - replaceStringPatch: replaces hardcoded redirect URI strings
    }
}
