package app.revanced.patches.shared.layout.branding

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val numberOfPresetAppNamesExtensionFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("I")
    parameters()
    custom { method, classDef ->
        method.name == "numberOfPresetAppNames" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}
