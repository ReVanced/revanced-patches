package app.revanced.patches.youtube.utils.overridequality.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object VideoQualityTextFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("[L", "I", "Z"),
    opcodes = listOf(
        Opcode.IF_GE,
        Opcode.AGET_OBJECT,
        Opcode.IGET_OBJECT
    ),
    strings = listOf("menu_item_video_quality")
)