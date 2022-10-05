package app.revanced.patches.youtube.misc.playertype.fingerprint

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.interaction.swipecontrols.annotation.SwipeControlsCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

//TODO constrain to only match in YoutubePlayerOverlaysLayout?
@Name("update-player-type-fingerprint")
@FuzzyPatternScanMethod(2)
@SwipeControlsCompatibility
@Version("0.0.1")
object UpdatePlayerTypeFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    opcodes = listOf(
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.IF_NE,
        Opcode.RETURN_VOID,
        Opcode.IPUT_OBJECT,
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.INVOKE_VIRTUAL,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.RETURN_VOID,
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID
    )
)
