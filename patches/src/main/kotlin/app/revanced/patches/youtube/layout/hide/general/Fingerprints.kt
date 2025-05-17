package app.revanced.patches.youtube.layout.hide.general

import app.revanced.patcher.checkCast
import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.string
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import app.revanced.patches.youtube.layout.searchbar.wideSearchbarLayoutFingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val hideShowMoreButtonFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        resourceLiteral("layout", "expand_button_down"),
        methodCall(smali = "Landroid/view/View;->inflate(Landroid/content/Context;ILandroid/view/ViewGroup;)Landroid/view/View;"),
        opcode(Opcode.MOVE_RESULT_OBJECT)
    )
}

internal val parseElementFromBufferFingerprint by fingerprint {
    parameters("L", "L", "[B", "L", "L")
    instructions(
        opcode(Opcode.IGET_OBJECT),
        // IGET_BOOLEAN // 20.07+
        opcode(Opcode.INVOKE_INTERFACE, maxAfter = 1),
        opcode(Opcode.MOVE_RESULT_OBJECT, maxAfter = 0),
        string("Failed to parse Element", partialMatch = true)
    )
}

internal val playerOverlayFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    instructions(
        string("player_overlay_in_video_programming")
    )
}

internal val showWatermarkFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L", "L")
}

/**
 * Matches same method as [wideSearchbarLayoutFingerprint].
 */
internal val yoodlesImageViewFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/view/View;")
    parameters("L", "L")
    instructions(
        resourceLiteral("id", "youtube_logo")
    )
}

internal val crowdfundingBoxFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IPUT_OBJECT,
    )
    literal { crowdfundingBoxId }
}

internal val albumCardsFingerprint by fingerprint {
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

internal val filterBarHeightFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    opcodes(
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IPUT,
    )
    literal { filterBarHeightId }
}

internal val relatedChipCloudFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    opcodes(
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
    )
    literal { relatedChipCloudMarginId }
}

internal val searchResultsChipBarFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    opcodes(
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
    )
    literal { barContainerHeightId }
}

internal val showFloatingMicrophoneButtonFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    instructions(
        resourceLiteral("id", "fab"),
        checkCast("/FloatingActionButton;", maxAfter = 10),
        opcode(Opcode.IGET_BOOLEAN, maxAfter = 10)
    )
}
