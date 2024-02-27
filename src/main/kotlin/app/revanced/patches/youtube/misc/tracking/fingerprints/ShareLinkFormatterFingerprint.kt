package app.revanced.patches.youtube.misc.tracking.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

/**
 * Sharing panel of YouTube
 *
 * This fingerprint is quite complex to be compatible with all versions from YouTube v18.25.40 to the latest version.
 * If you drop supporting the old version, please change the fingerprint to be more intuitive.
 */
object ShareLinkFormatterFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.IGET_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.GOTO,
        Opcode.MOVE_OBJECT,
        Opcode.INVOKE_VIRTUAL
    ),
    customFingerprint = custom@{ methodDef, _ ->
        if (methodDef.implementation == null)
            return@custom false

        var count = 0
        for (instruction in methodDef.implementation!!.instructions) {
            if (instruction.opcode != Opcode.SGET_OBJECT)
                continue

            val objectInstruction = instruction as ReferenceInstruction
            if ((objectInstruction.reference as FieldReference).name != "androidAppEndpoint")
                continue

            count++
        }
        count == 2
    }
)