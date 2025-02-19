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

/**
 * In some cases the extension code can be called before
 * the app main activity onCreate is called.
 *
 * This class is called from startup code titled "BPEA RunnableGuardLancet"
 */
internal val jatoInitHook = extensionHook(
    insertIndexResolver = { 0 },
    contextRegisterResolver = { "p1" }
) {
    custom { method, classDef ->
        classDef.type == "Lcom/ss/android/ugc/aweme/legoImp/task/JatoInitTask;" &&
                method.name == "run"
    }
}
