package app.revanced.patches.viber.misc.navbar
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.tabIdClassMethod by gettingFirstMethodDeclaratively {
    strings("shouldShowTabId")
}

context(BytecodePatchContext)
internal val shouldShowTabIdMethodFingerprint get() = fingerprint {
    parameterTypes("I", "I")
    returnType("Z")
    custom { methodDef, classDef ->
        classDef == tabIdClassMethod.classDef
    }
}
