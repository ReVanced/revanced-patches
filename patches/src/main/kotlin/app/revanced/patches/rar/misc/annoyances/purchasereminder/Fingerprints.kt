package app.revanced.patches.rar.misc.annoyances.purchasereminder

import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.showReminderMethod by gettingFirstMethodDeclaratively {
    definingClass { endsWith("AdsNotify;") }
    name("show")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("V")
}
