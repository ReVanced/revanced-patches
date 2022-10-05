package app.revanced.patches.youtube.layout.personalinformation.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.personalinformation.annotations.HideEmailAddressCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("account-switcher-accessibility-label-fingerprint")
@FuzzyPatternScanMethod(3)
@HideEmailAddressCompatibility
@Version("0.0.1")
object AccountSwitcherAccessibilityLabelFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.FINAL or AccessFlags.BRIDGE or AccessFlags.SYNTHETIC,
    listOf("L", "L"),
    listOf(
        Opcode.CHECK_CAST,
        Opcode.IGET_OBJECT,
        Opcode.NEW_INSTANCE,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_DIRECT,
        Opcode.CONST_4,
        Opcode.INVOKE_INTERFACE,
        Opcode.IGET,
        Opcode.CONST_4,
        Opcode.ADD_INT_2ADDR,
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT,
        Opcode.IF_NEZ,
        Opcode.SGET_OBJECT,
        Opcode.GOTO,
        Opcode.MOVE_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
    )
)