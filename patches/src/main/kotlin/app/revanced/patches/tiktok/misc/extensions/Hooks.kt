package app.revanced.patches.tiktok.misc.extensions

import app.revanced.patches.shared.misc.extensions.extensionsHook
import com.android.tools.smali.dexlib2.AccessFlags

internal val initHook = extensionsHook(
    insertIndexResolver = { 1 }, // Insert after call to super class.
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    custom { method, classDef ->
        classDef.endsWith("/AwemeHostApplication;") &&
            method.name == "<init>"
    }
}
