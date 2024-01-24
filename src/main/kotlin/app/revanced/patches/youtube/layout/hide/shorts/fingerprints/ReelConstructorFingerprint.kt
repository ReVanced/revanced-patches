package app.revanced.patches.youtube.layout.hide.shorts.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.youtube.layout.hide.shorts.HideShortsComponentsResourcePatch
import app.revanced.util.containsWideLiteralInstructionValue
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object ReelConstructorFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    opcodes = listOf(Opcode.INVOKE_VIRTUAL),
    customFingerprint = { methodDef, _ ->
        // Cannot use LiteralValueFingerprint, because the resource id may not be present.
        val reelMultipleItemShelfId = HideShortsComponentsResourcePatch.reelMultipleItemShelfId
        reelMultipleItemShelfId != -1L
                && methodDef.containsWideLiteralInstructionValue(reelMultipleItemShelfId)
    }
)