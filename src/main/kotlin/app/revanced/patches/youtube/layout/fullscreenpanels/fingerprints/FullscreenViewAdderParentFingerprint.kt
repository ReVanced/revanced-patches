package app.revanced.patches.youtube.layout.fullscreenpanels.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.shorts.button.annotations.ShortsButtonCompatibility
import org.jf.dexlib2.Opcode

@Name("fullscreen-view-adder-parent-fingerprint")
@MatchingMethod(
    "LFullscreenEngagementPanelOverlay;", "e"
)
@DirectPatternScanMethod
@ShortsButtonCompatibility
@Version("0.0.1")
object FullscreenViewAdderParentFingerprint : MethodFingerprint(
    null,
    null,
    listOf("L", "L"),
    listOf(
        Opcode.GOTO,
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQ,
        Opcode.GOTO,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
    ),
    null,
    { it.definingClass.endsWith("FullscreenEngagementPanelOverlay;") }
)
