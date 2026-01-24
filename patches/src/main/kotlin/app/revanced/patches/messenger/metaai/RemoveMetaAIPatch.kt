package app.revanced.patches.messenger.metaai

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.method
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.messenger.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.WideLiteralInstruction

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/messenger/metaai/RemoveMetaAIPatch;"
internal const val EXTENSION_METHOD_NAME = "overrideBooleanFlag"

@Suppress("unused", "ObjectPropertyName")
val `Remove Meta AI` by creatingBytecodePatch(
    description = "Removes UI elements related to Meta AI.",
) {
    compatibleWith("com.facebook.orca")

    dependsOn(sharedExtensionPatch)

    apply {
        getMobileConfigBoolMethod.apply {
            val returnIndex = getMobileConfigBoolMethod.patternMatch.startIndex // TODO
            val returnRegister = getInstruction<OneRegisterInstruction>(returnIndex).registerA

            addInstructions(
                returnIndex,
                """
                    invoke-static { p1, p2, v$returnRegister }, $EXTENSION_CLASS_DESCRIPTOR->$EXTENSION_METHOD_NAME(JZ)Z
                    move-result v$returnRegister
                """,
            )
        }

        // Extract the common starting digits of Meta AI flag IDs from a flag found in code.
        val relevantDigits = with(metaAIKillSwitchCheckMethod) {
            method.getInstruction<WideLiteralInstruction>(patternMatch.startIndex).wideLiteral // TODO
        }.toString().substring(0, 7)

        // Replace placeholder in the extension method.
        extensionMethodMethod.replaceInstruction(
            stringM.first().index, // TODO
            """
                    const-string v1, "$relevantDigits"
                """,
        )
    }
}
