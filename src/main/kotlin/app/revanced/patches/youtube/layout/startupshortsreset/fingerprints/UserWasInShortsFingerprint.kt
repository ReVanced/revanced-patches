package app.revanced.patches.youtube.layout.startupshortsreset.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.startupshortsreset.annotations.StartupShortsResetCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("user-was-in-shorts-fingerprint")
@FuzzyPatternScanMethod(3)
@StartupShortsResetCompatibility
@Version("0.0.1")
object UserWasInShortsFingerprint : MethodFingerprint(
    "V", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf("L"),
    opcodes = listOf(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT,
    ),
    strings = listOf("Failed to read user_was_in_shorts proto after successful warmup"),
)