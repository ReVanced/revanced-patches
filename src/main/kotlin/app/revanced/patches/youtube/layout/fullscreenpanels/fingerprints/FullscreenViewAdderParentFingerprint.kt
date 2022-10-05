package app.revanced.patches.youtube.layout.fullscreenpanels.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.pivotbar.shortsbutton.annotations.ShortsButtonCompatibility
import org.jf.dexlib2.Opcode

@Name("fullscreen-view-adder-parent-fingerprint")
@ShortsButtonCompatibility
@Version("0.0.1")
object FullscreenViewAdderParentFingerprint : MethodFingerprint(
    parameters = listOf("L", "L"),
    opcodes = listOf(
        Opcode.GOTO,
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQ,
        Opcode.GOTO,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
    ),
    customFingerprint = { it.definingClass.endsWith("FullscreenEngagementPanelOverlay;") }
)
