package app.revanced.patches.youtube.misc.litho.filter

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.lithoComponentNameUpbFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(45631264L())
}

internal val BytecodePatchContext.lithoConverterBufferUpbFeatureFlagMethodMatch by composingFirstMethod {
    returnType("L")
    instructions(45419603L())
}
