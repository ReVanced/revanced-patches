package app.revanced.patches.youtube.layout.returnyoutubedislike

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val conversionContextFingerprint = fingerprint {
    returns("Ljava/lang/String;")
    parameters()
    strings(
        ", widthConstraint=",
        ", heightConstraint=",
        ", templateLoggerFactory=",
        ", rootDisposableContainer=",
        // 18.37.36 and after this String is: ConversionContext{containerInternal=
        // and before it is: ConversionContext{container=
        // Use a partial string to match both.
        "ConversionContext{container",
    )
}

internal val dislikeFingerprint = fingerprint {
    returns("V")
    strings("like/dislike")
}

internal val dislikesOldLayoutTextViewFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    opcodes(
        Opcode.CONST, // resource identifier register
        Opcode.INVOKE_VIRTUAL,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.IF_NEZ, // textview register
        Opcode.GOTO,
    )
    literal { oldUIDislikeId }
}

internal val likeFingerprint = fingerprint {
    returns("V")
    strings("like/like")
}

internal val removeLikeFingerprint = fingerprint {
    returns("V")
    strings("like/removelike")
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
    strings("RollingNumberFontProperties{paint=")
}

internal val rollingNumberSetterFingerprint = fingerprint {
    opcodes(
        Opcode.INVOKE_DIRECT,
        Opcode.IGET_OBJECT,
    )
    strings("RollingNumberType required properties missing! Need updateCount, fontName, color and fontSize.")
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
        classDef.superclass == "Landroid/support/v7/widget/AppCompatTextView;"
    }
}

internal val shortsTextViewFingerprint = fingerprint {
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
        Opcode.SGET_OBJECT, // insertion point, must be after constructor call to parent class
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.CONST_4,
        Opcode.IF_EQ,
        Opcode.CONST_4,
        Opcode.IF_EQ,
        Opcode.RETURN_VOID,
        Opcode.IGET_OBJECT, // TextView field
        Opcode.IGET_BOOLEAN, // boolean field
    )
}

internal val textComponentConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.CONSTRUCTOR, AccessFlags.PRIVATE)
    strings("TextComponent")
}

internal val textComponentDataFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("L", "L")
    strings("text")
    custom { _, classDef ->
        val fields = classDef.fields
        fields.find { it.type == "Ljava/util/BitSet;" } != null &&
            fields.find { it.type == "[Ljava/lang/String;" } != null
    }
}

/**
 * Matches against the same class found in [textComponentConstructorFingerprint].
 */
internal val textComponentLookupFingerprint = fingerprint {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returns("L")
    parameters("L")
    strings("…")
}
