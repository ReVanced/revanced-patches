package app.revanced.patches.youtube.layout.returnyoutubedislike

import app.revanced.patcher.addString
import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val dislikeFingerprint = fingerprint {
    returnType("V")
    instructions(
        addString("like/dislike"),
    )
}

internal val likeFingerprint = fingerprint {
    returnType("V")
    instructions(
        addString("like/like"),
    )
}

internal val removeLikeFingerprint = fingerprint {
    returnType("V")
    instructions(
        addString("like/removelike"),
    )
}

internal val rollingNumberMeasureAnimatedTextFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Lj\$/util/Optional;")
    parameterTypes("L", "Ljava/lang/String;", "L")
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
    returnType("F")
    parameterTypes("Ljava/lang/String;")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.RETURN,
    )
}

internal val rollingNumberMeasureStaticLabelParentFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/String;")
    parameterTypes()
    instructions(
        addString("RollingNumberFontProperties{paint="),
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
    returnType("V")
    parameterTypes("L", "F", "F")
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
        addString("TextComponent"),
    )
}

internal val textComponentDataFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes("L", "L")
    instructions(
        addString("text"),
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
    returnType("L")
    parameterTypes("L")
    instructions(
        addString("…"),
    )
}

internal val textComponentFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(
        45675738L(),
    )
}
