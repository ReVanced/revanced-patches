package app.revanced.patches.strava.groupkudos

import app.revanced.patcher.firstMethod
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.initMethod by gettingFirstMethodDeclaratively {
    name("<init>")
    parameterTypes("Lcom/strava/feed/view/modal/GroupTabFragment;", "Z", "Landroidx/fragment/app/FragmentManager;")
}

context(_: BytecodePatchContext)
internal fun ClassDef.getActionHandlerMethod() = firstMethod("state")
