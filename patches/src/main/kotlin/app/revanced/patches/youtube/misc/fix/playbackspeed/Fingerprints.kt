package app.revanced.patches.youtube.misc.fix.playbackspeed

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionReversed
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

/**
 * This method is usually used to set the initial speed (1.0x) when playback starts from the feed.
 * For some reason, in the latest YouTube, it is invoked even after the video has already started.
 */
internal val playbackSpeedInFeedsFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    opcodes(
        Opcode.IGET,
        Opcode.MUL_INT_LIT16,
        Opcode.IGET_WIDE,
        Opcode.CONST_WIDE_16,
        Opcode.CMP_LONG,
        Opcode.IF_EQZ,
        Opcode.IF_LEZ,
        Opcode.SUB_LONG_2ADDR,
    )
    custom { method, _ ->
        indexOfGetPlaybackSpeedInstruction(method) >= 0
    }
}

internal fun indexOfGetPlaybackSpeedInstruction(method: Method) =
    method.indexOfFirstInstructionReversed {
        opcode == Opcode.IGET &&
                getReference<FieldReference>()?.type == "F"
    }
