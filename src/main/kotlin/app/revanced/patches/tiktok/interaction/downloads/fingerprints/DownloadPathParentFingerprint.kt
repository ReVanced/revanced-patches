package app.revanced.patches.tiktok.interaction.downloads.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object DownloadPathParentFingerprint : MethodFingerprint(
    "L",
    AccessFlags.PUBLIC or AccessFlags.STATIC,
    strings = listOf(
        "video/mp4"
    ),
    parameters = listOf(
        "L",
        "L"
    ),
    opcodes = listOf(
        Opcode.CONST_STRING,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT
    )
)