package app.revanced.patches.music.layout.premium.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object HideGetPremiumFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    listOf(),
    listOf(
        Opcode.IF_NEZ,
        Opcode.CONST_16,
        Opcode.GOTO,
        Opcode.NOP,
        Opcode.INVOKE_VIRTUAL,
    ),
    listOf("FEmusic_history", "FEmusic_offline"),
)
