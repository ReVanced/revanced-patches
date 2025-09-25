package app.revanced.patches.youtube.layout.hide.general

import app.revanced.patcher.fingerprint
import app.revanced.patches.youtube.layout.searchbar.wideSearchbarLayoutFingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val hideShowMoreButtonFingerprint = fingerprint {
    opcodes(
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
    )
    literal { expandButtonDownId }
}

/**
 * 20.12+
 */
internal val parseElementFromBufferFingerprint = fingerprint {
    parameters("L", "L", "[B", "L", "L")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
    )
    strings("Failed to parse Element") // String is a partial match.
}

/**
 * 20.07+
 */
internal val parseElementFromBufferLegacy2007Fingerprint = fingerprint {
    parameters("L", "L", "[B", "L", "L")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.IGET_BOOLEAN,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
    )
    strings("Failed to parse Element") // String is a partial match.
}

/**
 * 19.01 - 20.06
 */
internal val parseElementFromBufferLegacy1901Fingerprint = fingerprint {
    parameters("L", "L", "[B", "L", "L")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
    )
    strings("Failed to parse Element") // String is a partial match.
}

internal val playerOverlayFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    strings("player_overlay_in_video_programming")
}

internal val showWatermarkFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L", "L")
}

/**
 * Matches same method as [wideSearchbarLayoutFingerprint].
 */
internal val yoodlesImageViewFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    parameters("L", "L")
    literal { youTubeLogo }
}

internal val crowdfundingBoxFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IPUT_OBJECT,
    )
    literal { crowdfundingBoxId }
}

internal val albumCardsFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    opcodes(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
    )
    literal { albumCardId }
}

internal val filterBarHeightFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    opcodes(
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IPUT,
    )
    literal { filterBarHeightId }
}

internal val relatedChipCloudFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    opcodes(
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
    )
    literal { relatedChipCloudMarginId }
}

internal val searchResultsChipBarFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    opcodes(
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
    )
    literal { barContainerHeightId }
}

internal val showFloatingMicrophoneButtonFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    opcodes(
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQZ,
    )
    literal { fabButtonId }
}

internal val hideViewCountFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Ljava/lang/CharSequence;")

    opcodes(
        Opcode.RETURN_OBJECT,
        Opcode.CONST_STRING,
        Opcode.RETURN_OBJECT,
    )
    strings(
        "Has attachmentRuns but drawableRequester is missing.",
    )
}