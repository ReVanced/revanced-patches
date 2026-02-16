package app.revanced.patches.tudortmund.lockscreen

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.brightnessMethod by gettingFirstMethodDeclaratively {
    name("run")
    definingClass("/ScreenPlugin$")
    accessFlags(AccessFlags.PUBLIC)
    returnType("V")
    parameterTypes()
    custom { immutableClassDef.anyField { type == "Ljava/lang/Float;" } }
}
