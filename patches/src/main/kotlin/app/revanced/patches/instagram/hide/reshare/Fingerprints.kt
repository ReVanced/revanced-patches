package app.revanced.patches.instagram.hide.reshare

import app.revanced.patcher.fingerprint
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction31i
import com.android.tools.smali.dexlib2.iface.instruction.formats.SparseSwitchPayload

// The hash code of the field of interest. It is used as the key of a hashmap
internal val hashedFieldInteger = "enable_media_notes_production".hashCode()

internal val feedResponseMediaParserFingerprint = fingerprint {
    strings("array_out_of_bounds_exception", "null_pointer_exception", "MediaDict")
    custom { method, _ ->
        method.indexOfFirstInstruction {
            opcode == Opcode.SPARSE_SWITCH_PAYLOAD &&
                    (this as SparseSwitchPayload).switchElements.any { it.key == hashedFieldInteger }
        } >= 0
    }
}

internal val reelPostsResponseMediaParserFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    parameters("L", "L", "L", "[I")
    returns("L")
    custom { method, _ ->
        method.indexOfFirstInstruction {
            opcode == Opcode.CONST && (this as Instruction31i).narrowLiteral == hashedFieldInteger
        } >= 0
    }
}
