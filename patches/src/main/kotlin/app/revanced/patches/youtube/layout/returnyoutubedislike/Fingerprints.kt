package app.revanced.patches.youtube.layout.returnyoutubedislike

import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val dislikeFingerprint = fingerprint {
    returns("V")
    instructions(
        string("like/dislike")
    )
}

internal val likeFingerprint = fingerprint {
    returns("V")
    instructions(
        string("like/like")
    )
}

internal val removeLikeFingerprint = fingerprint {
    returns("V")
    instructions(
        string("like/removelike")
    )
}

internal val rollingNumberMeasureAnimatedTextFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Lj\$/util/Optional;")
    parameters("L", "Ljava/lang/String;", "L")
    opcodes(
        Opcode.IGET, // First instruction of method
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.CONST_HIGH16,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.CONST_4,
        Opcode.AGET,
        Opcode.CONST_4,
        Opcode.CONST_4, // Measured text width
    )
}

/**
 * Matches to class found in [rollingNumberMeasureStaticLabelParentFingerprint].
 */
internal val rollingNumberMeasureStaticLabelFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("F")
    parameters("Ljava/lang/String;")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.RETURN,
    )
}

internal val rollingNumberMeasureStaticLabelParentFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/String;")
    parameters()
    instructions(
        string("RollingNumberFontProperties{paint=")
    )
}

internal val rollingNumberSetterFingerprint = fingerprint {
    opcodes(
        Opcode.INVOKE_DIRECT,
        Opcode.IGET_OBJECT,
    )
    // Partial string match.
    strings("RollingNumberType required properties missing! Need")
}

internal val rollingNumberTextViewFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L", "F", "F")
    opcodes(
        Opcode.IPUT,
        null, // invoke-direct or invoke-virtual
        Opcode.IPUT_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID,
    )
    custom { _, classDef ->
        classDef.superclass == "Landroid/support/v7/widget/AppCompatTextView;" || classDef.superclass ==
                "Lcom/google/android/libraries/youtube/rendering/ui/spec/typography/YouTubeAppCompatTextView;"
    }
}

internal val textComponentConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.CONSTRUCTOR, AccessFlags.PRIVATE)
    instructions(
        string("TextComponent")
    )
}

internal val textComponentDataFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("L", "L")
    instructions(
        string("text")
    )
    custom { _, classDef ->
        classDef.fields.find { it.type == "Ljava/util/BitSet;" } != null
    }
}

/**
 * Matches against the same class found in [textComponentConstructorFingerprint].
 */
internal val textComponentLookupFingerprint = fingerprint {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returns("L")
    parameters("L")
    instructions(
        string("…")
    )
}

internal val textComponentFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.FINAL)
    returns("Z")
    parameters()
    instructions (
        literal(45675738L)
    )
}