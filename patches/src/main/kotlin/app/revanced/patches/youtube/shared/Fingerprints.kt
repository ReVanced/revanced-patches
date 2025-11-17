package app.revanced.patches.youtube.shared

import app.revanced.patcher.InstructionLocation.MatchAfterImmediately
import app.revanced.patcher.fieldAccess
import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.newInstance
import app.revanced.patcher.opcode
import app.revanced.patcher.string
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral
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
        ", identifierProperty="
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
        resourceLiteral(ResourceType.ID, "player_control_previous_button_touch_area"),
        resourceLiteral(ResourceType.ID, "player_control_next_button_touch_area"),
        methodCall(parameters = listOf("Landroid/view/View;", "I"))
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

internal val mainActivityOnCreateFingerprint = fingerprint {
    returns("V")
    parameters("Landroid/os/Bundle;")
    custom { method, classDef ->
        method.name == "onCreate" && classDef.type == YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE
    }
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
        string("timed_markers_width"),
    )
}

/**
 * Matches to _mutable_ class found in [seekbarFingerprint].
 */
internal val seekbarOnDrawFingerprint = fingerprint {
    instructions(
        methodCall(smali = "Ljava/lang/Math;->round(F)I"),
        opcode(Opcode.MOVE_RESULT, location = MatchAfterImmediately())
    )
    custom { method, _ -> method.name == "onDraw" }
}

internal val subtitleButtonControllerFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Lcom/google/android/libraries/youtube/player/subtitles/model/SubtitleTrack;")
    instructions(
        resourceLiteral(ResourceType.STRING, "accessibility_captions_unavailable"),
        resourceLiteral(ResourceType.STRING, "accessibility_captions_button_name"),
    )
}

internal val videoQualityChangedFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters("L")
    instructions(
        newInstance("Lcom/google/android/libraries/youtube/innertube/model/media/VideoQuality;"),
        opcode(Opcode.IGET_OBJECT),
        opcode(Opcode.CHECK_CAST),
        fieldAccess(type = "I", opcode = Opcode.IGET, location = MatchAfterImmediately()), // Video resolution (human readable).
    )
}
