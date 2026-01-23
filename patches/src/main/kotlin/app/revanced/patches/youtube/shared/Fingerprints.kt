package app.revanced.patches.youtube.shared

import app.revanced.patcher.InstructionLocation.MatchAfterImmediately
import app.revanced.patcher.accessFlags
import app.revanced.patcher.addString
import app.revanced.patcher.after
import app.revanced.patcher.allOf
import app.revanced.patcher.definingClass
import app.revanced.patcher.field
import app.revanced.patcher.fingerprint
import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.name
import app.revanced.patcher.opcode
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patcher.type
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal const val YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE = "Lcom/google/android/apps/youtube/app/watchwhile/MainActivity;"

internal val conversionContextFingerprintToString = fingerprint {
    parameters()
    strings(
        "ConversionContext{", // Partial string match.
        ", widthConstraint=",
        ", heightConstraint=",
        ", templateLoggerFactory=",
        ", rootDisposableContainer=",
        ", identifierProperty=",
    )
    custom { method, _ ->
        method.name == "toString"
    }
}

internal val layoutConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    instructions(
        literal(159962),
        ResourceType.ID("player_control_previous_button_touch_area"),
        ResourceType.ID("player_control_next_button_touch_area"),
        methodCall(parameters = listOf("Landroid/view/View;", "I")),
    )
}

internal val mainActivityConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters()
    custom { _, classDef ->
        classDef.type == YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE
    }
}

internal val mainActivityOnBackPressedFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    custom { method, classDef ->
        method.name == "onBackPressed" && classDef.type == YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE
    }
}

internal val BytecodePatchContext.mainActivityOnCreateMethod by gettingFirstMutableMethodDeclaratively {
    name("onCreate")
    definingClass(YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE)
    returnType("V")
    parameterTypes("Landroid/os/Bundle;")
}

internal val rollingNumberTextViewAnimationUpdateFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/graphics/Bitmap;")
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
    custom { _, classDef ->
        classDef.superclass == "Landroid/support/v7/widget/AppCompatTextView;" ||
            classDef.superclass ==
            "Lcom/google/android/libraries/youtube/rendering/ui/spec/typography/YouTubeAppCompatTextView;"
    }
}

internal val seekbarFingerprint = fingerprint {
    returns("V")
    instructions(
        addString("timed_markers_width"),
    )
}

/**
 * Matches to _mutable_ class found in [seekbarFingerprint].
 */
internal val seekbarOnDrawFingerprint = fingerprint {
    instructions(
        methodCall(smali = "Ljava/lang/Math;->round(F)I"),
        opcode(Opcode.MOVE_RESULT, location = MatchAfterImmediately()),
    )
    custom { method, _ -> method.name == "onDraw" }
}

internal val subtitleButtonControllerFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Lcom/google/android/libraries/youtube/player/subtitles/model/SubtitleTrack;")
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
