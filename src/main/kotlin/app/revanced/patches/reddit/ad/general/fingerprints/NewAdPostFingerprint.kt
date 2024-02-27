package app.revanced.patches.reddit.ad.general.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object NewAdPostFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    opcodes = listOf(
        Opcode.INVOKE_VIRTUAL,
    ),
    strings = listOf(
        "chain",
        "feedElement"
    ),
    customFingerprint = { _, classDef -> classDef.sourceFile == "AdElementConverter.kt" },
)