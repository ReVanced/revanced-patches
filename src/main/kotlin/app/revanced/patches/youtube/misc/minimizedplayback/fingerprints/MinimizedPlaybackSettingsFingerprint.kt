package app.revanced.patches.youtube.misc.minimizedplayback.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.misc.minimizedplayback.MinimizedPlaybackResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object MinimizedPlaybackSettingsFingerprint : LiteralValueFingerprint(
    returnType = "Ljava/lang/String;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf(),
    opcodes = listOf(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.IF_NEZ,
        Opcode.GOTO
    ),
    literalSupplier = { MinimizedPlaybackResourcePatch.prefBackgroundAndOfflineCategoryId }
)
