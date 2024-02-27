package app.revanced.patches.youtube.general.searchterm

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.general.searchterm.fingerprints.CreateSearchSuggestionsFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.GENERAL
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    name = "Hide search term thumbnail",
    description = "Adds an option to hide thumbnails in the search term history.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object SearchTermThumbnailPatch : BytecodePatch(
    setOf(CreateSearchSuggestionsFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        CreateSearchSuggestionsFingerprint.result?.let { result ->
            result.mutableMethod.apply {
                val instructions = implementation!!.instructions

                val relativeIndex = instructions.indexOfFirst { instruction ->
                    instruction.opcode == Opcode.CONST_16
                            && (instruction as NarrowLiteralInstruction).narrowLiteral == 40
                }

                val replaceIndex =
                    getTargetIndexDownTo(
                        relativeIndex,
                        Opcode.INVOKE_VIRTUAL,
                        "Landroid/widget/ImageView;->setVisibility(I)V"
                    ) - 1

                val jumpIndex =
                    getTargetIndexUpTo(
                        relativeIndex,
                        Opcode.INVOKE_STATIC,
                        "Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;"
                    ) + 4

                val replaceIndexInstruction = getInstruction<TwoRegisterInstruction>(replaceIndex)
                val replaceIndexReference =
                    getInstruction<ReferenceInstruction>(replaceIndex).reference

                addInstructionsWithLabels(
                    replaceIndex + 1, """
                        invoke-static { }, $GENERAL->hideSearchTermThumbnail()Z
                        move-result v${replaceIndexInstruction.registerA}
                        if-nez v${replaceIndexInstruction.registerA}, :hidden
                        iget-object v${replaceIndexInstruction.registerA}, v${replaceIndexInstruction.registerB}, $replaceIndexReference
                        """, ExternalLabel("hidden", getInstruction(jumpIndex))
                )
                removeInstruction(replaceIndex)
            }
        } ?: throw CreateSearchSuggestionsFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: GENERAL_SETTINGS",
                "SETTINGS: HIDE_SEARCH_TERM_THUMBNAIL"
            )
        )

        SettingsPatch.updatePatchStatus("Hide search term thumbnail")

    }

    private fun MutableMethod.getTargetIndexDownTo(
        startIndex: Int,
        opcode: Opcode,
        reference: String
    ): Int {
        for (index in startIndex downTo 0) {
            if (getInstruction(index).opcode != opcode)
                continue

            val targetReference = getInstruction<ReferenceInstruction>(index).reference.toString()
            if (targetReference != reference)
                continue

            return index
        }
        throw PatchException("Failed to find hook method")
    }

    private fun MutableMethod.getTargetIndexUpTo(
        startIndex: Int,
        opcode: Opcode,
        reference: String
    ): Int {
        for (index in startIndex until implementation!!.instructions.size) {
            if (getInstruction(index).opcode != opcode)
                continue

            val targetReference = getInstruction<ReferenceInstruction>(index).reference.toString()
            if (targetReference != reference)
                continue

            return index
        }
        throw PatchException("Failed to find hook method")
    }
}
