package app.revanced.patches.viber.misc.navbar

import app.revanced.patcher.firstMutableMethodDeclaratively
import app.revanced.patcher.gettingFirstMethod
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.tabIdClassMethod by gettingFirstMethod("shouldShowTabId")

context(_: BytecodePatchContext)
internal fun ClassDef.getShouldShowTabIdMethod() = firstMutableMethodDeclaratively {
    parameterTypes("I", "I")
    returnType("Z")
}
