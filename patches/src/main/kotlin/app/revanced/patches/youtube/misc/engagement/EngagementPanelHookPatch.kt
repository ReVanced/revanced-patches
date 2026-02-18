package app.revanced.patches.youtube.misc.engagement

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.shared.getEngagementPanelControllerMethodMatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/shared/EngagementPanel;"

val engagementPanelHookPatch = bytecodePatch(
    description = "Hook to get the current engagement panel state.",
) {
    dependsOn(sharedExtensionPatch)

    apply {
        val match = getEngagementPanelControllerMethodMatch()
        match.method.apply {
            val panelIdField = getInstruction(match[-1]).getReference<FieldReference>()!!
            val insertIndex = match[5]

            val (freeRegister, panelRegister) =
                with(getInstruction<TwoRegisterInstruction>(insertIndex)) {
                    Pair(registerA, registerB)
                }

            addInstructions(
                insertIndex,
                """
                    iget-object v$freeRegister, v$panelRegister, $panelIdField
                    invoke-static { v$freeRegister }, $EXTENSION_CLASS_DESCRIPTOR->open(Ljava/lang/String;)V
                """
            )
        }

        match.immutableClassDef.getEngagementPanelUpdateMethod().addInstruction(
            0,
            "invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->close()V"
        )
    }
}
