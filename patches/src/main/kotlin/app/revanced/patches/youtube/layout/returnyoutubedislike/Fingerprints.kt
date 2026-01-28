package app.revanced.patches.youtube.layout.returnyoutubedislike

import app.revanced.patcher.*
import app.revanced.patcher.extensions.instructions
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.dislikeMethod by gettingFirstMutableMethodDeclaratively {
    returnType("V")
    instructions("like/dislike"())
}

internal val BytecodePatchContext.likeMethod by gettingFirstMutableMethodDeclaratively {
    returnType("V")
    instructions("like/like"())
}

internal val BytecodePatchContext.removeLikeMethod by gettingFirstMutableMethodDeclaratively {
    returnType("V")
    instructions("like/removelike"())
}

internal val rollingNumberMeasureAnimatedTextMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Lj\$/util/Optional;")
    parameterTypes("L", "Ljava/lang/String;", "L")
    opcodes(
        Opcode.IGET, // First instruction of method.
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.CONST_HIGH16,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.CONST_4,
        Opcode.AGET,
        Opcode.CONST_4,
        Opcode.CONST_4, // Measured text width.
    )
}

/**
 * Matches to class found in [rollingNumberMeasureStaticLabelParentMethod].
 */
internal val rollingNumberMeasureStaticLabelMethod = firstMethodComposite {
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

internal val BytecodePatchContext.rollingNumberMeasureStaticLabelParentMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/String;")
    parameterTypes()
    instructions(
        "RollingNumberFontProperties{paint="(),
    )
}

internal val rollingNumberSetterMethodMatch = firstMethodComposite {
    opcodes(
        Opcode.INVOKE_DIRECT,
        Opcode.IGET_OBJECT,
    )
    custom {
        instructions.matchIndexed(
            "string",
            "RollingNumberType required properties missing! Need"(String::contains),
        )
    }
}

internal val BytecodePatchContext.rollingNumberTextViewMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L", "F", "F")
    instructions(
        Opcode.IPUT(),
        anyOf(Opcode.INVOKE_DIRECT(), Opcode.INVOKE_VIRTUAL()),
        Opcode.IPUT_OBJECT(),
        Opcode.IGET_OBJECT(),
        Opcode.INVOKE_VIRTUAL(),
        Opcode.RETURN_VOID(),
    )
    custom {
        immutableClassDef.superclass == "Landroid/support/v7/widget/AppCompatTextView;" || immutableClassDef.superclass ==
            "Lcom/google/android/libraries/youtube/rendering/ui/spec/typography/YouTubeAppCompatTextView;"
    }
}

internal val BytecodePatchContext.textComponentConstructorMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.CONSTRUCTOR, AccessFlags.PRIVATE)
    instructions(
        "TextComponent"(),
    )
}

internal val BytecodePatchContext.textComponentDataMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes("L", "L")
    instructions(
        "text"(),
    )
    custom { immutableClassDef.anyField { type == "Ljava/util/BitSet;" } }
}

/**
 * Matches against the same class found in [textComponentConstructorMethod].
 */
context(_: BytecodePatchContext)
internal fun ClassDef.getTextComponentLookupMethod() = firstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returnType("L")
    parameterTypes("L")
    instructions("…"())
}

internal val BytecodePatchContext.textComponentFeatureFlagMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(45675738L())
}
