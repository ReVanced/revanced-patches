package app.revanced.patches.youtube.layout.hide.general

import app.revanced.patcher.*
import app.revanced.patcher.after
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef


internal val BytecodePatchContext.hideShowMoreButtonSetViewMethodMatch by composingFirstMethod {
    returnType("V")

    var methodDefiningClass = ""
    custom {
        methodDefiningClass = definingClass
        true
    }
    instructions(
        ResourceType.ID("link_text_start"),
        allOf(
            Opcode.IPUT_OBJECT(),
            field { type == "Landroid/widget/TextView;" && definingClass == methodDefiningClass }),
        ResourceType.ID("expand_button_container"),
        allOf(
            Opcode.IPUT_OBJECT(),
            field { type == "Landroid/view/View;" && definingClass == methodDefiningClass })
    )
}

context(_: BytecodePatchContext)
internal fun ClassDef.getHideShowMoreButtonGetParentViewMethod() =
    firstImmutableMethodDeclaratively {
        accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
        returnType("Landroid/view/View;")
        parameterTypes()
    }

context(_: BytecodePatchContext)
internal fun ClassDef.getHideShowMoreButtonMethod() = firstMethodDeclaratively {
    returnType("V")
    parameterTypes("L", "Ljava/lang/Object;")
    instructions(
        allOf(
            Opcode.INVOKE_VIRTUAL(),
            method {
                toString() == "Landroid/view/View;->setContentDescription(Ljava/lang/CharSequence;)V"
            }
        )
    )
}


/**
 * 20.21+
 */
internal val BytecodePatchContext.hideSubscribedChannelsBarConstructorMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        ResourceType.ID("parent_container"),
        afterAtMost(3, Opcode.MOVE_RESULT_OBJECT()),
        afterAtMost(
            5,
            allOf(Opcode.NEW_INSTANCE(), type($$"Landroid/widget/LinearLayout$LayoutParams;"))
        )
    )
    custom { immutableClassDef.anyField { type == "Landroid/support/v7/widget/RecyclerView;" } }
}

/**
 * ~ 20.21
 */
internal val BytecodePatchContext.hideSubscribedChannelsBarConstructorLegacyMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        ResourceType.ID("parent_container"),
        afterAtMost(3, Opcode.MOVE_RESULT_OBJECT()),
        afterAtMost(
            5,
            allOf(Opcode.NEW_INSTANCE(), type($$"Landroid/widget/LinearLayout$LayoutParams;"))
        )
    )
}

internal val ClassDef.hideSubscribedChannelsBarLandscapeMethodMatch by ClassDefComposing.composingFirstMethod {
    returnType("V")
    parameterTypes()
    instructions(
        ResourceType.DIMEN("parent_view_width_in_wide_mode"),
        allOf(Opcode.INVOKE_VIRTUAL(), method("getDimensionPixelSize")),
        after(Opcode.MOVE_RESULT())
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

internal val BytecodePatchContext.searchBoxTypingStringMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L")
    instructions(
        allOf(Opcode.IGET_OBJECT(), field { type == "Ljava/util/Collection;" }),
        afterAtMost(5, method { toString() == "Ljava/util/ArrayList;-><init>(Ljava/util/Collection;)V" }),
        allOf(Opcode.IGET_OBJECT(), field { type == "Ljava/lang/String;" }),
        afterAtMost(5, method { toString() == "Ljava/lang/String;->isEmpty()Z" }),
        ResourceType.DIMEN("suggestion_category_divider_height")
    )
}

internal val BytecodePatchContext.searchSuggestionEndpointConstructorMethod by gettingFirstImmutableMethodDeclaratively(
    "\u2026 "
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returnType("V")
}

internal val ClassDef.searchSuggestionEndpointMethodMatch by ClassDefComposing.composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()

    var methodDefiningClass = ""
    custom {
        methodDefiningClass = definingClass
        true
    }

    instructions(
        allOf(
            Opcode.IGET_OBJECT(),
            field { definingClass == methodDefiningClass && type == "Ljava/lang/String;" }),
        allOf(
            Opcode.INVOKE_STATIC(),
            method { toString() == "Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z" }),
    )
}

internal val BytecodePatchContext.latestVideosContentPillMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L", "Z")
    instructions(
        ResourceType.LAYOUT("content_pill"),
        method {
            toString() == "Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;"
        },
        after(Opcode.MOVE_RESULT_OBJECT())
    )
}

internal val BytecodePatchContext.latestVideosBarMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L", "Z")
    instructions(
        ResourceType.LAYOUT("bar"),
        method {
            toString() == "Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;"
        },
        after(Opcode.MOVE_RESULT_OBJECT())
    )
}


internal val BytecodePatchContext.bottomSheetMenuItemBuilderMethodMatch by composingFirstMethod {
    returnType("L")
    parameterTypes("L")
    instructions(
        allOf(
            Opcode.INVOKE_STATIC(),
            method {
                returnType == "Ljava/lang/CharSequence;" &&
                        parameterTypes.size == 1 && parameterTypes[0].startsWith("L")
            }
        ),
        after(Opcode.MOVE_RESULT_OBJECT()),
        "Text missing for BottomSheetMenuItem."()
    )
}

internal val BytecodePatchContext.contextualMenuItemBuilderMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.SYNTHETIC)
    returnType("V")
    parameterTypes("L", "L")
    instructions(
        allOf(Opcode.CHECK_CAST(), type("Landroid/widget/TextView;")),
        afterAtMost(
            5,
            method { toString() == "Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V" }
        ),
        ResourceType.DIMEN("poster_art_width_default"),
    )
}

internal val BytecodePatchContext.channelTabBuilderMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Landroid/view/View;")
    parameterTypes(
        "Ljava/lang/CharSequence;",
        "Ljava/lang/CharSequence;",
        "Z",
        "L"
    )
}

internal val BytecodePatchContext.channelTabRendererMethod by gettingFirstMethodDeclaratively(
    "TabRenderer.content contains SectionListRenderer but the tab does not have a section list controller."
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes(
        "L",
        "Ljava/util/List;",
        "I"
    )
}