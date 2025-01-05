package app.revanced.patches.youtube.layout.returnyoutubedislike

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val conversionContextFingerprint by fingerprint {
    returns("Ljava/lang/String;")
    parameters()
    strings(
        ", widthConstraint=",
        ", heightConstraint=",
        ", templateLoggerFactory=",
        ", rootDisposableContainer=",
        "ConversionContext{containerInternal=",
    )
}

internal val dislikeFingerprint by fingerprint {
    returns("V")
    strings("like/dislike")
}

internal val likeFingerprint by fingerprint {
    returns("V")
    strings("like/like")
}

internal val removeLikeFingerprint by fingerprint {
    returns("V")
    strings("like/removelike")
}

internal val rollingNumberMeasureAnimatedTextFingerprint by fingerprint {
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
internal val rollingNumberMeasureStaticLabelFingerprint by fingerprint {
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

internal val rollingNumberMeasureStaticLabelParentFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/String;")
    parameters()
    strings("RollingNumberFontProperties{paint=")
}

internal val rollingNumberSetterFingerprint by fingerprint {
    opcodes(
        Opcode.INVOKE_DIRECT,
        Opcode.IGET_OBJECT,
    )
    // Partial string match.
    strings("RollingNumberType required properties missing! Need")
}

internal val rollingNumberTextViewFingerprint by fingerprint {
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

internal val shortsTextViewFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L", "L")
    opcodes(
        Opcode.INVOKE_SUPER, // first instruction of method
        Opcode.IF_NEZ,
        null,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
    )
}

internal val textComponentConstructorFingerprint by fingerprint {
    accessFlags(AccessFlags.CONSTRUCTOR, AccessFlags.PRIVATE)
    strings("TextComponent")
}

internal val textComponentDataFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("L", "L")
    strings("text")
    custom { _, classDef ->
        classDef.fields.find { it.type == "Ljava/util/BitSet;" } != null
    }
}

/**
 * Matches against the same class found in [textComponentConstructorFingerprint].
 */
internal val textComponentLookupFingerprint by fingerprint {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returns("L")
    parameters("L")
    strings("…")
}
