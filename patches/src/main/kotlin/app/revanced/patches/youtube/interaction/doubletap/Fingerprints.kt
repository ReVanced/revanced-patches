package app.revanced.patches.youtube.interaction.doubletap

import app.revanced.patcher.accessFlags
import app.revanced.patcher.firstMutableMethodDeclaratively
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.seekTypeEnumMethod by gettingFirstMethodDeclaratively(
    "SEEK_SOURCE_SEEK_TO_NEXT_CHAPTER",
    "SEEK_SOURCE_SEEK_TO_PREVIOUS_CHAPTER",
) {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
}

context(_: BytecodePatchContext)
internal fun ClassDef.getDoubleTapInfoCtorMethod() = firstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes(
        "Landroid/view/MotionEvent;",
        "I",
        "Z",
        "Lj$/time/Duration;",
    )
}
