package app.revanced.patches.youtube.player.infocards.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object InfoCardsIncognitoFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/Boolean;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L", "J"),
    opcodes = listOf(Opcode.IGET_BOOLEAN),
    strings = listOf("vibrator")
)