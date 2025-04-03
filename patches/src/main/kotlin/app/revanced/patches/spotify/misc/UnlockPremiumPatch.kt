package app.revanced.patches.spotify.misc

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.util.*
import app.revanced.util.findFreeRegister
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction10t
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/spotify/misc/UnlockPremiumPatch;"

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Spotify Premium",
    description = "Unlocks Spotify Premium features. Server-sided features like downloading songs are still locked.",
) {
    compatibleWith("com.spotify.music")

    dependsOn(sharedExtensionPatch)

    execute {
        // Make _value accessible so that it can be overridden in the extension.
        accountAttributeFingerprint.classDef.fields.first { it.name == "value_" }.apply {
            // Add public flag and remove private.
            accessFlags = accessFlags.or(AccessFlags.PUBLIC.value).and(AccessFlags.PRIVATE.value.inv())
        }

        // Override the attributes map in the getter method.
        with(productStateProtoFingerprint.method) {
            val getAttributesMapIndex = indexOfFirstInstructionOrThrow(Opcode.IGET_OBJECT)
            val attributesMapRegister = getInstruction<TwoRegisterInstruction>(getAttributesMapIndex).registerA

            addInstruction(
                getAttributesMapIndex + 1,
                "invoke-static { v$attributesMapRegister }, $EXTENSION_CLASS_DESCRIPTOR->overrideAttribute(Ljava/util/Map;)V"
            )
        }

        // Add the query parameter trackRows to show popular tracks in the artist page.
        with(buildQueryParametersFingerprint) {
            val addQueryParameterConditionIndex = method.indexOfFirstInstructionReversedOrThrow(stringMatches!!.first().index, Opcode.IF_EQZ)
            method.replaceInstruction(addQueryParameterConditionIndex, "nop")
        }

        // Disable the "Spotify Premium" upsell experiment in context menus.
        with(contextMenuExperimentsFingerprint) {
            val moveIsEnabledIndex = method.indexOfFirstInstructionOrThrow(stringMatches!!.first().index, Opcode.MOVE_RESULT)
            val isUpsellEnabledRegister = method.getInstruction<OneRegisterInstruction>(moveIsEnabledIndex).registerA
            method.replaceInstruction(moveIsEnabledIndex, "const/4 v$isUpsellEnabledRegister, 0")
        }

        // Make featureTypeCase_ acessible so we can check the home section type in the extension.
        homeSectionFingerprint.classDef.fields.first { it.name == "featureTypeCase_" }.apply {
            accessFlags = accessFlags.or(AccessFlags.PUBLIC.value).and(AccessFlags.PRIVATE.value.inv())
        }

        // Remove ads sections from home.
        with(mapHomeSectionFingerprint.method) {
            val sectionCastIndex = indexOfFirstInstructionOrThrow {
                if (opcode != Opcode.CHECK_CAST) {
                    return@indexOfFirstInstructionOrThrow false
                }

                val reference = getReference<TypeReference>()
                reference?.type?.endsWith("homeapi/proto/Section;") ?: false
            }

            val sectionRegister = getInstruction<OneRegisterInstruction>(sectionCastIndex).registerA
            val freeRegister = findFreeRegister(sectionCastIndex, sectionRegister)

            val iteratorHasNextIndex = indexOfFirstInstructionReversedOrThrow(sectionCastIndex) {
                if (opcode != Opcode.INVOKE_INTERFACE) {
                    return@indexOfFirstInstructionReversedOrThrow false
                }

                val reference = getReference<MethodReference>()
                reference?.name == "hasNext"
            }

            val sectionClassName = "Lcom/spotify/home/evopage/homeapi/proto/Section;"

            addInstructionsWithLabels(
                sectionCastIndex + 1,
                """
                    # Continue to mapping next section if the current section needs to be removed.

                    invoke-static { v$sectionRegister }, $EXTENSION_CLASS_DESCRIPTOR->isRemovedHomeSection($sectionClassName)Z
                    move-result v$freeRegister
                    if-nez v$freeRegister, :map_section_start
                """,
                ExternalLabel("map_section_start", getInstruction(iteratorHasNextIndex))
            )
        }
    }
}
