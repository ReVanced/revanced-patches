package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.returnyoutubedislike.ReturnYouTubeDislikeResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object DislikesOldLayoutTextViewFingerprint : LiteralValueFingerprint(
    returnType = "V",
    parameters = listOf("L"),
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    opcodes = listOf(
        Opcode.CONST, // resource identifier register
        Opcode.INVOKE_VIRTUAL,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.IF_NEZ, // textview register
        Opcode.GOTO,
    ),
    literalSupplier = { ReturnYouTubeDislikeResourcePatch.oldUIDislikeId }
)