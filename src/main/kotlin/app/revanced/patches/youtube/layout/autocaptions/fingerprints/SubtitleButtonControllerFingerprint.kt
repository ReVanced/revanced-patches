package app.revanced.patches.youtube.layout.autocaptions.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.autocaptions.annotations.AutoCaptionsCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("subtitle-button-controller-fingerprint")
@AutoCaptionsCompatibility
@Version("0.0.1")
object SubtitleButtonControllerFingerprint : MethodFingerprint(
    "V", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf("L"), listOf(
        Opcode.IGET_OBJECT,
        Opcode.IF_NEZ,
        Opcode.RETURN_VOID,
        Opcode.IGET_BOOLEAN,
        Opcode.CONST_4,
        Opcode.IF_NEZ,
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
    )
)