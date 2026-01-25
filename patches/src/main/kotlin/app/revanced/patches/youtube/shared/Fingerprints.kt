package app.revanced.patches.youtube.shared

import app.revanced.patcher.accessFlags
import app.revanced.patcher.after
import app.revanced.patcher.allOf
import app.revanced.patcher.custom
import app.revanced.patcher.definingClass
import app.revanced.patcher.field
import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.firstMethodDeclaratively
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.instruction
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.method
import app.revanced.patcher.name
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patcher.type
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal const val YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE = "Lcom/google/android/apps/youtube/app/watchwhile/MainActivity;"

internal val BytecodePatchContext.conversionContextToStringMethod by gettingFirstMethodDeclaratively(
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

internal fun getLayoutConstructorMethodMatch() = firstMethodComposite {
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
                parameterTypes.zip(methodParameterTypePrefixes).all { (a, b) -> a.startsWith(b) }
        },
    )
}

internal val BytecodePatchContext.mainActivityConstructorMethod by gettingFirstMethodDeclaratively {
    definingClass(YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE)
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes()
}

internal val BytecodePatchContext.mainActivityOnBackPressedMethod by gettingFirstMutableMethodDeclaratively {
    name("onBackPressed")
    definingClass(YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE)
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes()
}

internal val BytecodePatchContext.mainActivityOnCreateMethod by gettingFirstMutableMethodDeclaratively {
    name("onCreate")
    definingClass(YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE)
    returnType("V")
    parameterTypes("Landroid/os/Bundle;")
}

internal val BytecodePatchContext.rollingNumberTextViewAnimationUpdateMethod by gettingFirstMethodDeclaratively {
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

internal val BytecodePatchContext.seekbarMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    instructions("timed_markers_width"())
}

/**
 * Matches to _mutable_ class found in [seekbarMethod].
 */
internal fun getSeekbarOnDrawMethodMatch() = firstMethodComposite {
    name("onDraw")
    instructions(
        method { toString() == "Ljava/lang/Math;->round(F)I" },
        after(Opcode.MOVE_RESULT()),
    )
}

internal val BytecodePatchContext.subtitleButtonControllerMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Lcom/google/android/libraries/youtube/player/subtitles/model/SubtitleTrack;")
    instructions(
        ResourceType.STRING("accessibility_captions_unavailable"),
        ResourceType.STRING("accessibility_captions_button_name"),
    )
}

internal val videoQualityChangedMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes("L")
    instructions(
        allOf(Opcode.NEW_INSTANCE(), type("Lcom/google/android/libraries/youtube/innertube/model/media/VideoQuality;")),
        Opcode.IGET_OBJECT(),
        Opcode.CHECK_CAST(),
        after(allOf(Opcode.IGET(), field { type == "I" })), // Video resolution (human-readable).
    )
}
