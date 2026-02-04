package app.revanced.patches.spotify.layout.theme

import app.revanced.patcher.accessFlags
import app.revanced.patcher.firstMethodDeclaratively
import app.revanced.patcher.gettingFirstImmutableMethod
import app.revanced.patcher.gettingFirstImmutableMethodDeclaratively
import app.revanced.patcher.gettingFirstMethod
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patcher.unorderedAllOf
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.colorSpaceUtilsClassMethod by gettingFirstImmutableMethodDeclaratively {
    instructions("The specified color must be encoded in an RGB color space."(String::contains))
}

context(_: BytecodePatchContext)
internal fun ClassDef.getConvertArgbToRgbaMethod() = firstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
    returnType("J")
    parameterTypes("J")
}

internal val BytecodePatchContext.parseLottieJsonMethod by gettingFirstMethod("Unsupported matte type: ")

internal val BytecodePatchContext.parseAnimatedColorMethod by gettingFirstMethodDeclaratively {
    parameterTypes("L", "F")
    returnType("Ljava/lang/Object;")
    instructions(predicates = unorderedAllOf(255.0.toRawBits()(), 1.0.toRawBits()()))
}
