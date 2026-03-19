package app.revanced.patches.youtube.layout.hide.endscreensuggestedvideo

import app.revanced.patcher.*
import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.methodReference
import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.instructions
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

internal val BytecodePatchContext.autoNavConstructorMethod by gettingFirstImmutableMethodDeclaratively(
    "main_app_autonav"
) {
    returnType("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
}

context(_: BytecodePatchContext)
internal fun ClassDef.getAutoNavStatusMethod() = firstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
}

internal val BytecodePatchContext.removeOnLayoutChangeListenerMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes()
    instructions(
        allOf(Opcode.IPUT(), field { type == "I" }),
        afterAtMost(
            3,
            allOf(
                Opcode.INVOKE_VIRTUAL(),
                method { returnType == "V" && parameterTypes.isEmpty() }
            )
        ),
        allOf(Opcode.INVOKE_VIRTUAL(), method {
            name == "removeOnLayoutChangeListener" &&
                    returnType == "V" &&
                    definingClass == "Lcom/google/android/apps/youtube/app/common/" +
                    "player/overlay/YouTubePlayerOverlaysLayout;"
        }),
    )
}

internal fun BytecodePatchContext.getEndScreenSuggestedVideoMethodMatch(autoNavStatusMethod: Method): CompositeMatch {
    val endScreenMethod = removeOnLayoutChangeListenerMethodMatch.let {
        firstMethod(it.method.getInstruction<ReferenceInstruction>(it[1]).methodReference!!)
    }

    return firstMethodComposite {
        name(endScreenMethod.name)
        definingClass(endScreenMethod.definingClass)
        accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
        returnType("V")
        parameterTypes()
        instructions(
            allOf(
                Opcode.IGET_OBJECT(),
                field {
                    definingClass == endScreenMethod.definingClass &&
                            type == autoNavStatusMethod.definingClass
                }
            ),
            afterAtMost(
                3,
                allOf(
                    Opcode.INVOKE_VIRTUAL(),
                    method { this == autoNavStatusMethod }
                )
            ),
        )
    }
}
