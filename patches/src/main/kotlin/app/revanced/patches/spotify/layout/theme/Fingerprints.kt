package app.revanced.patches.spotify.layout.theme

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.util.containsLiteralInstruction
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.colorSpaceUtilsClassMethod by gettingFirstMethodDeclaratively {
    strings("The specified color must be encoded in an RGB color space.") // Partial string match.
}

internal val BytecodePatchContext.convertArgbToRgbaMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
    returnType("J")
    parameterTypes("J")
}

internal val BytecodePatchContext.parseLottieJsonMethod by gettingFirstMethodDeclaratively {
    strings("Unsupported matte type: ")
}

internal val BytecodePatchContext.parseAnimatedColorMethod by gettingFirstMethodDeclaratively {
    parameterTypes("L", "F")
    returnType("Ljava/lang/Object;")
    custom { method, _ ->
        method.containsLiteralInstruction(255.0) &&
            method.containsLiteralInstruction(1.0)
    }
}
