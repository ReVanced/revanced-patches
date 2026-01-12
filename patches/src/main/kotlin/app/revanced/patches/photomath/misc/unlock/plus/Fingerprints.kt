package app.revanced.patches.photomath.misc.unlock.plus

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.isPlusUnlockedMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    string("genius")
    definingClass("/User;"::endsWith)
}
