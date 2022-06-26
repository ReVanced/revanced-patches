package app.revanced.patches.youtube.layout.shorts.button.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.shorts.button.annotations.ShortsButtonCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("pivotbar-buttons-view-fingerprint")
@MatchingMethod(
    "Lknw;", "z"
)
@FuzzyPatternScanMethod(2) // FIXME: Test this threshold and find the best value.
@ShortsButtonCompatibility
@Version("0.0.1")
object PivotBarButtonsViewFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    listOf("Z"),
    listOf(
        Opcode.NEW_INSTANCE, // new StateListDrawable()
        Opcode.INVOKE_DIRECT,
        Opcode.NEW_ARRAY,
        Opcode.CONST,
        Opcode.CONST_16,
        Opcode.APUT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.SGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_OBJECT,
        Opcode.MOVE_OBJECT,
        Opcode.MOVE,
        Opcode.MOVE_OBJECT,
        Opcode.INVOKE_VIRTUAL_RANGE, // pivotBar.getView(drawable, tabName, z, i, map, akebVar, optional)
        Opcode.MOVE_RESULT_OBJECT,
    )
)