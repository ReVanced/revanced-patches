package app.revanced.patches.youtube.layout.hide.general

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.youtube.layout.searchbar.wideSearchbarLayoutMethod
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

/**
 * 20.26+
 */
internal val BytecodePatchContext.hideShowMoreButtonMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.SYNTHETIC)
    returnType("V")
    parameterTypes("L", "Ljava/lang/Object;")
    instructions(
        ResourceType.LAYOUT("expand_button_down"),
        method { toString() == "Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;" },
        after(Opcode.MOVE_RESULT_OBJECT()),
    )
}

internal val BytecodePatchContext.hideShowMoreLegacyButtonMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        ResourceType.LAYOUT("expand_button_down"),
        method { toString() == "Landroid/view/View;->inflate(Landroid/content/Context;ILandroid/view/ViewGroup;)Landroid/view/View;" },
        Opcode.MOVE_RESULT_OBJECT(),
    )
}

internal val BytecodePatchContext.parseElementFromBufferMethodMatch by composingFirstMethod {
    parameterTypes("L", "L", "[B", "L", "L")
    instructions(
        Opcode.IGET_OBJECT(),
        // IGET_BOOLEAN // 20.07+
        afterAtMost(1, Opcode.INVOKE_INTERFACE()),
        after(Opcode.MOVE_RESULT_OBJECT()),
        "Failed to parse Element"(String::startsWith),
    )
}

internal val BytecodePatchContext.playerOverlayMethod by gettingFirstImmutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    instructions(
        "player_overlay_in_video_programming"(),
    )
}

context(_: BytecodePatchContext)
internal fun ClassDef.getShowWatermarkMethod() = firstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L", "L")
}

/**
 * Matches same method as [wideSearchbarLayoutMethod].
 */
internal val BytecodePatchContext.yoodlesImageViewMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Landroid/view/View;")
    parameterTypes("L", "L")
    instructions(ResourceType.ID("youtube_logo"))
}

internal val BytecodePatchContext.crowdfundingBoxMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    opcodes(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IPUT_OBJECT,
    )
    literal { crowdfundingBoxId }
}

internal val BytecodePatchContext.albumCardsMethodMatch by composingFirstMethod {
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

internal val BytecodePatchContext.filterBarHeightMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    opcodes(
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IPUT,
    )
    literal { filterBarHeightId }
}

internal val BytecodePatchContext.relatedChipCloudMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    opcodes(
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
    )
    literal { relatedChipCloudMarginId }
}

internal val BytecodePatchContext.searchResultsChipBarMethodMatch by composingFirstMethod {
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

internal val BytecodePatchContext.showFloatingMicrophoneButtonMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes()
    instructions(
        ResourceType.ID("fab"),
        afterAtMost(10, allOf(Opcode.CHECK_CAST(), type { endsWith("/FloatingActionButton;") })),
        afterAtMost(10, Opcode.IGET_BOOLEAN()),
    )
}

internal val BytecodePatchContext.hideViewCountMethodMatch by composingFirstMethod(
    "Has attachmentRuns but drawableRequester is missing.",
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Ljava/lang/CharSequence;")
    opcodes(
        Opcode.RETURN_OBJECT,
        Opcode.CONST_STRING,
        Opcode.RETURN_OBJECT,
    )
}
