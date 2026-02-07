package app.revanced.patches.youtube.shared

import app.revanced.patcher.accessFlags
import app.revanced.patcher.after
import app.revanced.patcher.afterAtMost
import app.revanced.patcher.allOf
import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.custom
import app.revanced.patcher.definingClass
import app.revanced.patcher.field
import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.gettingFirstImmutableMethodDeclaratively
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.method
import app.revanced.patcher.name
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

internal const val YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE =
    "Lcom/google/android/apps/youtube/app/watchwhile/MainActivity;"

internal val BytecodePatchContext.conversionContextToStringMethod by gettingFirstImmutableMethodDeclaratively(
    ", widthConstraint=",
    ", heightConstraint=",
    ", templateLoggerFactory=",
    ", rootDisposableContainer=",
    ", identifierProperty=",
) {
    name("toString")
    parameterTypes()
    instructions("ConversionContext{"(String::startsWith)) // Partial string match.
}

internal val BytecodePatchContext.backgroundPlaybackManagerShortsMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Z")
    parameterTypes("L")
    instructions(151635310L())
}

internal fun BytecodePatchContext.getEngagementPanelControllerMethodMatch() = firstMethodComposite {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returnType("L")
    parameterTypes("L", "L", "Z", "Z")
    instructions(
        "EngagementPanelController: cannot show EngagementPanel before EngagementPanelController.init() has been called."(),
        method { toString() == "Lj$/util/Optional;->orElse(Ljava/lang/Object;)Ljava/lang/Object;" },
        method { toString() == "Lj$/util/Optional;->orElse(Ljava/lang/Object;)Ljava/lang/Object;" },
        afterAtMost(4, Opcode.CHECK_CAST()),
        after(Opcode.IF_EQZ()),
        after(Opcode.IGET_OBJECT()),
        45615449L(),
        method { toString() == "Ljava/util/ArrayDeque;->iterator()Ljava/util/Iterator;" },
        afterAtMost(10, allOf(Opcode.IGET_OBJECT(), field { type == "Ljava/lang/String;" }))
    )
}


internal fun BytecodePatchContext.getLayoutConstructorMethodMatch() = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes()

    val methodParameterTypePrefixes = listOf("Landroid/view/View;", "I")

    instructions(
        159962L(),
        ResourceType.ID("player_control_previous_button_touch_area"),
        ResourceType.ID("player_control_next_button_touch_area"),
        method {
            parameterTypes.size == 2 &&
                    parameterTypes.zip(methodParameterTypePrefixes)
                        .all { (a, b) -> a.startsWith(b) }
        },
    )
}

internal val BytecodePatchContext.mainActivityConstructorMethod by gettingFirstImmutableMethodDeclaratively {
    definingClass(YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE)
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes()
}

internal val BytecodePatchContext.mainActivityOnBackPressedMethod by gettingFirstMethodDeclaratively {
    name("onBackPressed")
    definingClass(YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE)
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes()
}

internal val BytecodePatchContext.mainActivityOnCreateMethod by gettingFirstMethodDeclaratively {
    name("onCreate")
    definingClass(YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE)
    returnType("V")
    parameterTypes("Landroid/os/Bundle;")
}

internal val BytecodePatchContext.rollingNumberTextViewAnimationUpdateMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Landroid/graphics/Bitmap;")
    opcodes(
        Opcode.NEW_INSTANCE, // bitmap ImageSpan
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_4,
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.CONST_16,
        Opcode.INVOKE_VIRTUAL,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.INT_TO_FLOAT,
        Opcode.INVOKE_VIRTUAL, // set textview padding using bitmap width
    )
    custom {
        immutableClassDef.superclass == "Landroid/support/v7/widget/AppCompatTextView;" ||
                immutableClassDef.superclass ==
                "Lcom/google/android/libraries/youtube/rendering/ui/spec/typography/YouTubeAppCompatTextView;"
    }
}

internal val BytecodePatchContext.seekbarMethod by gettingFirstImmutableMethodDeclaratively {
    returnType("V")
    instructions("timed_markers_width"())
}

internal fun ClassDef.getSeekbarOnDrawMethodMatch() = firstMethodComposite {
    name("onDraw")
    instructions(
        method { toString() == "Ljava/lang/Math;->round(F)I" },
        after(Opcode.MOVE_RESULT()),
    )
}

internal val BytecodePatchContext.subtitleButtonControllerMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L")
    instructions(
        ResourceType.STRING("accessibility_captions_unavailable"),
        ResourceType.STRING("accessibility_captions_button_name"),
    )
}

internal val BytecodePatchContext.videoQualityChangedMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes("L")
    instructions(
        allOf(Opcode.IGET(), field { type == "I" }),
        after(2L()),
        after(Opcode.IF_NE()),
        after(Opcode.NEW_INSTANCE()), // Obfuscated VideoQuality.
        afterAtMost(6, Opcode.IGET_OBJECT()),
        Opcode.CHECK_CAST(),
        after(allOf(Opcode.IGET(), field { type == "I" })), // Video resolution (human-readable).
    )
}


internal fun BytecodePatchContext.getToolBarButtonMethodMatch() = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    instructions(
        ResourceType.ID("menu_item_view"),
        allOf(Opcode.INVOKE_INTERFACE(), method { returnType == "I" }),
        after(Opcode.MOVE_RESULT()),
        afterAtMost(6, allOf(Opcode.IGET_OBJECT(), field { type == "Landroid/widget/ImageView;" })),
        afterAtMost(
            8,
            method { name == "getDrawable" && definingClass == "Landroid/content/res/Resources;" }),
        afterAtMost(
            4,
            method { name == "setImageDrawable" && definingClass == "Landroid/widget/ImageView;" }),
    )
    // 20.37+ has second parameter of "Landroid/content/Context;"
    custom { parameterTypes.count() in 1..2 && parameterTypes.first() == "Landroid/view/MenuItem;" }
}