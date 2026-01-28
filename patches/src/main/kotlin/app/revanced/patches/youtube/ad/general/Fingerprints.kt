package app.revanced.patches.youtube.ad.general

import app.revanced.patcher.accessFlags
import app.revanced.patcher.custom
import app.revanced.patcher.gettingFirstMethodDeclaratively
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
    custom {
        containsLiteralInstruction(fullScreenEngagementAdContainer) &&
            indexOfAddListInstruction(this) >= 0
    }
}

internal fun indexOfAddListInstruction(method: Method) = method.indexOfFirstInstructionReversed {
    getReference<MethodReference>()?.name == "add"
}
