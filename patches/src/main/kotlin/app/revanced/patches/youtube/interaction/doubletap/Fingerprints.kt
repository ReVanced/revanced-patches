package app.revanced.patches.youtube.interaction.doubletap

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.seekTypeEnumMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    strings(
        "SEEK_SOURCE_SEEK_TO_NEXT_CHAPTER",
        "SEEK_SOURCE_SEEK_TO_PREVIOUS_CHAPTER",
    )
}

internal val BytecodePatchContext.doubleTapInfoCtorMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes(
        "Landroid/view/MotionEvent;",
        "I",
        "Z",
        "Lj\$/time/Duration;",
    )
}
