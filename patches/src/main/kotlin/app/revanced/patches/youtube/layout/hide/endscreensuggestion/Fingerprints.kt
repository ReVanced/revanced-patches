package app.revanced.patches.youtube.layout.hide.endscreensuggestion

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val BytecodePatchContext.autoNavConstructorMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    strings("main_app_autonav")
}

internal val BytecodePatchContext.autoNavStatusMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
}

internal val BytecodePatchContext.removeOnLayoutChangeListenerMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes()
    opcodes(
        Opcode.IPUT,
        Opcode.INVOKE_VIRTUAL,
    )
    // This is the only reference present in the entire smali.
    custom { method, _ ->
        method.indexOfFirstInstruction {
            val reference = getReference<MethodReference>()
            reference?.name == "removeOnLayoutChangeListener" &&
                reference.definingClass.endsWith("/YouTubePlayerOverlaysLayout;")
        } >= 0
    }
}
