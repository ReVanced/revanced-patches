package app.revanced.patches.youtube.misc.litho.filter.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.litho.filter.annotation.LithoFilterCompatibility
import org.jf.dexlib2.Opcode

@Name("component-context-parser-fingerprint")
@LithoFilterCompatibility
@Version("0.0.1")
object ComponentContextParserFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.GOTO,
        Opcode.INVOKE_VIRTUAL,
        Opcode.CONST_16,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IPUT_OBJECT,
        Opcode.NEW_INSTANCE
    ),
    strings = listOf("LoggingProperties are not in proto format")
)