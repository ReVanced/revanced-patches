package app.revanced.patches.music.misc.tastebuilder.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object TasteBuilderSyntheticFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL or AccessFlags.SYNTHETIC,
    parameters = listOf("L", "Ljava/lang/Object;"),
    opcodes = listOf(
        Opcode.IF_NEZ,
        Opcode.IGET_OBJECT
    )
)
