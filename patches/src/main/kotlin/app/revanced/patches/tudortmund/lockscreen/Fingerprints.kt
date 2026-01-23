package app.revanced.patches.tudortmund.lockscreen

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.brightnessMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC)
    returnType("V")
    parameterTypes()
    custom { method, classDef ->
        method.name == "run" &&
            method.definingClass.contains("/ScreenPlugin\$") &&
            classDef.fields.any { it.type == "Ljava/lang/Float;" }
    }
}
