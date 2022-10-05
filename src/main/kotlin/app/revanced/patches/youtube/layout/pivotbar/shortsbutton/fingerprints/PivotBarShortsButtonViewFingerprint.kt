package app.revanced.patches.youtube.layout.pivotbar.shortsbutton.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.pivotbar.shortsbutton.annotations.ShortsButtonCompatibility
import org.jf.dexlib2.Opcode

@Name("pivot-bar-shorts-button-view-fingerprint")
@ShortsButtonCompatibility
@Version("0.0.1")
object PivotBarShortsButtonViewFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_VIRTUAL_RANGE, // unique instruction anchor
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.GOTO,
    )
)