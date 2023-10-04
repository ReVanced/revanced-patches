package app.revanced.patches.youtube.layout.hide.shorts.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.hide.shorts.HideShortsComponentsResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object ReelConstructorFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    opcodes = listOf(Opcode.INVOKE_VIRTUAL),
    literalSupplier = { HideShortsComponentsResourcePatch.reelMultipleItemShelfId }
)