package app.revanced.patches.viber.misc.navbar

import app.revanced.patcher.firstMethodDeclaratively
import app.revanced.patcher.gettingFirstImmutableMethod
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.tabIdClassMethod by gettingFirstImmutableMethod("shouldShowTabId")

context(_: BytecodePatchContext)
internal fun ClassDef.getShouldShowTabIdMethod() = firstMethodDeclaratively {
    parameterTypes("I", "I")
    returnType("Z")
}
