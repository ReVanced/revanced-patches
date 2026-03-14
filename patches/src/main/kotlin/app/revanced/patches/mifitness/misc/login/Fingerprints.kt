package app.revanced.patches.mifitness.misc.login

import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.xiaomiAccountManagerConstructorMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.CONSTRUCTOR)
    parameterTypes("Landroid/content/Context;", "Z")
    definingClass("Lcom/xiaomi/passport/accountmanager/XiaomiAccountManager;")
}
