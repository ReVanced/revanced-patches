package app.revanced.patches.youtube.fullscreen.fullscreenpanels.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

object FullscreenViewAdderFingerprint : MethodFingerprint(
    parameters = listOf("L", "L"),
    opcodes = listOf(
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQ,
        Opcode.GOTO,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL
    ),
    customFingerprint = { methodDef, _ -> methodDef.definingClass.endsWith("/FullscreenEngagementPanelOverlay;") }
)