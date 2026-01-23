package app.revanced.patches.viber.misc.navbar
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.BytecodePatchContext

internal val tabIdClassFingerprint = fingerprint {
    strings("shouldShowTabId")
}

context(BytecodePatchContext)
internal val shouldShowTabIdMethodFingerprint get() = fingerprint {
    parameterTypes("I", "I")
    returnType("Z")
    custom { methodDef, classDef ->
        classDef == tabIdClassFingerprint.classDef
    }
}
