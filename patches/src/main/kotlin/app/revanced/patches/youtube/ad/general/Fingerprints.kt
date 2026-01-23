package app.revanced.patches.youtube.ad.general

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.util.containsLiteralInstruction
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionReversed
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val BytecodePatchContext.fullScreenEngagementAdContainerMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes()
    custom { method, _ ->
        method.containsLiteralInstruction(fullScreenEngagementAdContainer) &&
            indexOfAddListInstruction(method) >= 0
    }
}

internal fun indexOfAddListInstruction(method: Method) = method.indexOfFirstInstructionReversed {
    getReference<MethodReference>()?.name == "add"
}
