package app.revanced.patches.youtube.shared

import app.revanced.patcher.fieldAccess
import app.revanced.patcher.fingerprint
import app.revanced.patcher.newInstance
import app.revanced.patcher.opcode
import app.revanced.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val autoRepeatFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    custom { method, _ ->
        method.implementation!!.instructions.count() == 3 && method.annotations.isEmpty()
    }
}

internal val autoRepeatParentFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    instructions(
        string("play() called when the player wasn't loaded."),
        string("play() blocked because Background Playability failed")
    )
}

internal val layoutConstructorFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    instructions(
        string("1.0x"),
    )
}

internal val mainActivityOnCreateFingerprint by fingerprint {
    returns("V")
    parameters("Landroid/os/Bundle;")
    custom { method, classDef ->
        method.name == "onCreate" && classDef.endsWith("MainActivity;")
    }
}

internal val rollingNumberTextViewAnimationUpdateFingerprint by fingerprint {
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

internal val seekbarFingerprint by fingerprint {
    returns("V")
    instructions(
        string("timed_markers_width"),
    )
}

internal val seekbarOnDrawFingerprint by fingerprint {
    custom { method, _ -> method.name == "onDraw" }
}

internal val subtitleButtonControllerFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Lcom/google/android/libraries/youtube/player/subtitles/model/SubtitleTrack;")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.IF_NEZ,
        Opcode.RETURN_VOID,
        Opcode.IGET_BOOLEAN,
        Opcode.CONST_4,
        Opcode.IF_NEZ,
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
    )
}

internal val newVideoQualityChangedFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters("L")
    instructions(
        newInstance("Lcom/google/android/libraries/youtube/innertube/model/media/VideoQuality;"),
        opcode(Opcode.IGET_OBJECT),
        opcode(Opcode.CHECK_CAST),
        fieldAccess(type = "I", opcode = Opcode.IGET, maxAfter = 0), // Video resolution (human readable).
        fieldAccess(type = "Ljava/lang/String;", opcode = Opcode.IGET_OBJECT, maxAfter = 0),
    )
}
