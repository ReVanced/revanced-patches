package app.revanced.patches.strava.media.download

import app.revanced.patcher.accessFlags
import app.revanced.patcher.firstMutableMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.ClassDef

context(_: BytecodePatchContext)
internal fun ClassDef.getCreateAndShowFragmentMethod() = firstMutableMethodDeclaratively("mediaType") {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L")
}

context(_: BytecodePatchContext)
internal fun ClassDef.getHandleMediaActionMethod() = firstMutableMethodDeclaratively {
    parameterTypes("Landroid/view/View;", "Lcom/strava/bottomsheet/BottomSheetItem;")
}
