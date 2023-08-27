package app.revanced.patches.youtube.layout.panels.fullscreen.remove.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object FullscreenViewAdderParentFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Landroid/view/View;"),
    opcodes = listOf(
        Opcode.GOTO,
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQ,
        Opcode.GOTO,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
    ),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("FullscreenEngagementPanelOverlay;")
    }
)
