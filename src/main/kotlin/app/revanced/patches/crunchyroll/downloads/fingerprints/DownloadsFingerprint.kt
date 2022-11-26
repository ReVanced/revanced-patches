package app.revanced.patches.crunchyroll.downloads.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.crunchyroll.downloads.annotations.DownloadsCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("downloads-fingerprint")
@DownloadsCompatibility
@Version("0.0.1")
object DownloadsFingerprint : MethodFingerprint(
    "Z", AccessFlags.PUBLIC or AccessFlags.FINAL, null,
    opcodes = listOf(
        Opcode.CONST_STRING,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.RETURN
    ),
    strings = listOf("offline_viewing"),
)