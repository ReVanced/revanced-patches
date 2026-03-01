package app.revanced.patches.youtube.misc.contexthook

import app.revanced.patcher.ClassDefComposing
import app.revanced.patcher.accessFlags
import app.revanced.patcher.after
import app.revanced.patcher.afterAtMost
import app.revanced.patcher.allOf
import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.custom
import app.revanced.patcher.extensions.methodReference
import app.revanced.patcher.field
import app.revanced.patcher.gettingFirstImmutableMethodDeclaratively
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.method
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.google.common.io.ByteArrayDataOutput

internal const val CLIENT_INFO_CLASS_DESCRIPTOR =
    $$"Lcom/google/protos/youtube/api/innertube/InnertubeContext$ClientInfo;"

internal val BytecodePatchContext.authenticationChangeListenerMethod by gettingFirstMethodDeclaratively(
    "Authentication changed while request was being made"
) {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returnType("V")
    custom { indexOfMessageLiteBuilderReference(this) >= 0 }
}

internal fun indexOfMessageLiteBuilderReference(method: Method, type: String = "L") =
    method.indexOfFirstInstruction {
        val reference = methodReference
        opcode == Opcode.INVOKE_VIRTUAL &&
                reference?.parameterTypes?.isEmpty() == true && reference.returnType.startsWith(type)
    }

internal val BytecodePatchContext.buildClientContextBodyConstructorMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returnType("V")
    instructions(
        "Android Wear"(),
        Opcode.IF_EQZ(),
        after("Android Automotive"()),
        "Android"(),
        after(allOf(Opcode.IPUT_OBJECT(), field()))
    )
}

internal val ClassDef.buildClientContextBodyMethodMatch by ClassDefComposing.composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes()
    instructions(
        allOf(Opcode.SGET(), field("SDK_INT")),
        allOf(
            Opcode.IPUT_OBJECT(),
            field { definingClass == CLIENT_INFO_CLASS_DESCRIPTOR && type == "Ljava/lang/String;" }
        ),
        Opcode.OR_INT_LIT16()
    )
}

internal val BytecodePatchContext.buildDummyClientContextBodyMethodMatch by composingFirstMethod {
    instructions(
        allOf(
            Opcode.IGET_OBJECT(),
            field("instance")
        ),
        afterAtMost(10, "10.29"()),
        allOf(
            Opcode.IPUT_OBJECT(),
            field { definingClass == CLIENT_INFO_CLASS_DESCRIPTOR && type == "Ljava/lang/String;" }
        ),
        allOf(
            Opcode.IPUT_OBJECT(),
            field { type == CLIENT_INFO_CLASS_DESCRIPTOR }
        )
    )
}

internal val ClassDef.browseEndpointConstructorMethodMatch by ClassDefComposing.composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returnType("V")

    var methodDefiningClass = ""
    custom {
        methodDefiningClass = this.definingClass
        true
    }

    instructions(
        ""(),
        after(
            allOf(
                Opcode.IPUT_OBJECT(),
                field { definingClass == methodDefiningClass && type == "Ljava/lang/String;" }
            )
        ),
    )
}

internal val BytecodePatchContext.browseEndpointParentMethod by gettingFirstImmutableMethodDeclaratively(
    "browseId"
) {
    returnType("Ljava/lang/String;")
}

internal val BytecodePatchContext.guideEndpointConstructorMethod by gettingFirstImmutableMethodDeclaratively(
    "guide"
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returnType("V")
}

internal val BytecodePatchContext.reelCreateItemsEndpointConstructorMethod by gettingFirstImmutableMethodDeclaratively(
    "reel/create_reel_items"
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returnType("V")
}

internal val BytecodePatchContext.reelItemWatchEndpointConstructorMethod by gettingFirstImmutableMethodDeclaratively(
    "reel/reel_item_watch"
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returnType("V")
}

internal val BytecodePatchContext.reelWatchSequenceEndpointConstructorMethod by gettingFirstImmutableMethodDeclaratively(
    "reel/reel_watch_sequence"
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returnType("V")
}

internal val BytecodePatchContext.searchRequestBuildParametersMethod by gettingFirstImmutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/String;")
    parameterTypes()
    instructions(
        "searchFormData"(),
        after(allOf(Opcode.INVOKE_VIRTUAL(), method("toByteArray"))),
        after(Opcode.MOVE_RESULT_OBJECT())
    )
}
