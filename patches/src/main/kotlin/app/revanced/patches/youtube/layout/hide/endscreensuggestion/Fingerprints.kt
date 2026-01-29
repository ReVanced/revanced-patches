package app.revanced.patches.youtube.layout.hide.endscreensuggestion

import app.revanced.patcher.*
import app.revanced.patcher.extensions.instructions
import app.revanced.patcher.extensions.methodReference
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.autoNavConstructorMethod by gettingFirstMethodDeclaratively("main_app_autonav") {
    returnType("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
}

context(_: BytecodePatchContext)
internal fun ClassDef.getAutoNavStatusMethod() = firstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
}

internal val BytecodePatchContext.removeOnLayoutChangeListenerMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes()
    opcodes(
        Opcode.IPUT,
        Opcode.INVOKE_VIRTUAL,
    )
    // This is the only reference present in the entire smali.
    custom {
        instructions.anyInstruction {
            val reference = methodReference
            reference?.name == "removeOnLayoutChangeListener" && reference.definingClass.endsWith("/YouTubePlayerOverlaysLayout;")
        }
    }
}
