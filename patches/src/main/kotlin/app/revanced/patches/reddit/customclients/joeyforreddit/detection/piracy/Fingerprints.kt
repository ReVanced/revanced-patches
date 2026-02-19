package app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy

import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.opcodes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.detectPiracyMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("V")
    opcodes(
        Opcode.NEW_INSTANCE,
        Opcode.CONST_16,
        Opcode.CONST_WIDE_16,
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID,
    )
    definingClass { endsWith("ProcessLifeCyleListener;") }
}
