package app.revanced.patches.spotify.misc

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.util.*
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

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
            val addQueryParameterConditionIndex = method.indexOfFirstInstructionReversedOrThrow(
                stringMatches!!.first().index, Opcode.IF_EQZ
            )
            method.replaceInstruction(addQueryParameterConditionIndex, "nop")
        }

        // Disable the "Spotify Premium" upsell experiment in context menus.
        with(contextMenuExperimentsFingerprint) {
            val moveIsEnabledIndex = method.indexOfFirstInstructionOrThrow(
                stringMatches!!.first().index, Opcode.MOVE_RESULT
            )
            val isUpsellEnabledRegister = method.getInstruction<OneRegisterInstruction>(moveIsEnabledIndex).registerA
            method.replaceInstruction(moveIsEnabledIndex, "const/4 v$isUpsellEnabledRegister, 0")
        }

        // Make featureTypeCase_ accessible so we can check the home section type in the extension.
        homeSectionFingerprint.classDef.fields.first { it.name == "featureTypeCase_" }.apply {
            accessFlags = accessFlags.or(AccessFlags.PUBLIC.value).and(AccessFlags.PRIVATE.value.inv())
        }

        val protobufListClassName = with(protobufListsFingerprint.originalMethod) {
            val emptyProtobufListGetIndex = indexOfFirstInstructionOrThrow(Opcode.SGET_OBJECT)
            getInstruction(emptyProtobufListGetIndex).getReference<FieldReference>()!!.definingClass
        }

        val protobufListRemoveFigerprint = fingerprint {
            custom { m, c ->
                m.name == "remove" && c.type == protobufListClassName
            }
        }

        // Make protobufList remove method not throw an error when the list is unmodifiable.
        // The patch below uses the remove method to remove ads sections from home.
        with(protobufListRemoveFigerprint.method) {
            val invokeThrowUnmodifiableIndex = indexOfFirstInstructionOrThrow {
                if (opcode != Opcode.INVOKE_VIRTUAL) {
                    return@indexOfFirstInstructionOrThrow false
                }

                val reference = getReference<MethodReference>()
                reference?.returnType == "V" && reference.parameterTypes.size == 0
            }

            removeInstruction(invokeThrowUnmodifiableIndex)
        }

        // Remove ads sections from home.
        with(homeStructureFingerprint.method) {
            val getSectionsIndex = indexOfFirstInstructionOrThrow(Opcode.IGET_OBJECT)
            val sectionsRegister = getInstruction<TwoRegisterInstruction>(getSectionsIndex).registerA

            addInstruction(
                getSectionsIndex + 1,
                "invoke-static { v$sectionsRegister }, $EXTENSION_CLASS_DESCRIPTOR->removeHomeSections(Ljava/util/List;)V"
            )
        }
    }
}
