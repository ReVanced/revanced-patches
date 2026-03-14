package app.revanced.patches.youtube.misc.engagement

import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.fieldReference
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.shared.getEngagementPanelControllerMethodMatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import kotlin.properties.Delegates

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/shared/EngagementPanel;"

var panelControllerMethod: MutableMethod by Delegates.notNull()
    private set
var panelIdIndex = 0
    private set
var panelIdRegister = 0
    private set
var panelIdSmaliInstruction = ""
    private set

val engagementPanelHookPatch = bytecodePatch(
    description = "Hook to get the current engagement panel state.",
) {
    dependsOn(sharedExtensionPatch)

    apply {
        val match = getEngagementPanelControllerMethodMatch()
        match.method.apply {
            val panelIdField = getInstruction(match[-1]).fieldReference
            val insertIndex = match[5]

            val (freeRegister, panelRegister) =
                with(getInstruction<TwoRegisterInstruction>(insertIndex)) {
                    Pair(registerA, registerB)
                }

            panelControllerMethod = this
            panelIdIndex = insertIndex
            panelIdRegister = freeRegister
            panelIdSmaliInstruction =
                "iget-object v$panelIdRegister, v$panelRegister, $panelIdField"

            addInstructions(
                insertIndex,
                """
                        $panelIdSmaliInstruction
                        invoke-static { v${panelIdRegister} }, ${EXTENSION_CLASS_DESCRIPTOR}->open(Ljava/lang/String;)V
                    """
            )
        }

        match.immutableClassDef.getEngagementPanelUpdateMethod().addInstruction(
            0,
            "invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->close()V"
        )
    }
}


fun addEngagementPanelIdHook(descriptor: String) = panelControllerMethod.addInstructionsWithLabels(
    panelIdIndex,
    """
        $panelIdSmaliInstruction
        invoke-static { v$panelIdRegister }, $descriptor
        move-result v$panelIdRegister
        if-eqz v$panelIdRegister, :shown
        const/4 v$panelIdRegister, 0x0
        return-object v$panelIdRegister
        :shown
        nop
    """
)
