package app.revanced.patches.youtube.layout.autocaptions.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.autocaptions.annotations.AutoCaptionsCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("start-video-informer-fingerprint")
@FuzzyPatternScanMethod(3)
@AutoCaptionsCompatibility
@Version("0.0.1")
object StartVideoInformerFingerprint : MethodFingerprint(
    "V", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf("L", "L", "L", "L"), listOf(
        Opcode.INVOKE_STATIC,
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.IF_EQZ,
        Opcode.CONST_STRING,
        Opcode.INVOKE_INTERFACE,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.CONST_4,
        Opcode.IF_EQ,
        Opcode.GOTO,
    )
)