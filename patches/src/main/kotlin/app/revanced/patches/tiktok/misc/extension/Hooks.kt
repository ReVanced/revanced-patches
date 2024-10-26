package app.revanced.patches.tiktok.misc.extension

import app.revanced.patches.shared.misc.extension.extensionHook
import com.android.tools.smali.dexlib2.AccessFlags

internal val initHook = extensionHook(
    insertIndexResolver = { 1 }, // Insert after call to super class.
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    custom { method, classDef ->
        classDef.endsWith("/AwemeHostApplication;") &&
            method.name == "<init>"
    }
}
