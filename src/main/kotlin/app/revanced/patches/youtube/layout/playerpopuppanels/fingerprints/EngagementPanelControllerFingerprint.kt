package app.revanced.patches.youtube.layout.playerpopuppanels.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.playerpopuppanels.annotations.PlayerPopupPanelsCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("engagement-player-controller-fingerprint")
@FuzzyPatternScanMethod(3)
@PlayerPopupPanelsCompatibility
@Version("0.0.1")
object EngagementPanelControllerFingerprint : MethodFingerprint(
    "L", AccessFlags.PRIVATE or AccessFlags.FINAL, listOf("L", "L", "Z", "Z", "Z"), listOf(
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.MOVE_FROM16,
        Opcode.IGET_BOOLEAN,
        Opcode.CONST_4,
        Opcode.IF_NEZ,
        Opcode.CONST_STRING,
        Opcode.INVOKE_STATIC,
        Opcode.SGET_OBJECT,
        Opcode.SGET_OBJECT,
        Opcode.CONST_STRING,
        Opcode.INVOKE_STATIC,
        Opcode.RETURN_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.CONST_4,
        Opcode.CONST_4,
        Opcode.CONST_4,
        Opcode.CONST_4,
        Opcode.CONST_4,
        Opcode.CONST_4,
    )
)