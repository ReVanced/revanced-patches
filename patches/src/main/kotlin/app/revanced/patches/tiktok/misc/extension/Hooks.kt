package app.revanced.patches.tiktok.misc.extension

import app.revanced.patches.shared.misc.extension.extensionHook
import com.android.tools.smali.dexlib2.AccessFlags

internal val initHook = extensionHook {
    custom { method, classDef ->
        classDef.type == "Lcom/ss/android/ugc/aweme/main/MainActivity;" &&
                method.name == "onCreate"
    }
}

/**
 * In some cases the extension code can be called before
 * the app main activity onCreate is called.
 *
 * This class is called from startup code titled "BPEA RunnableGuardLancet".
 */
internal val jatoInitHook = extensionHook(
    contextRegisterResolver = { "p1" }
) {
    parameters("Landroid/content/Context;")
    custom { method, classDef ->
        classDef.type == "Lcom/ss/android/ugc/aweme/legoImp/task/JatoInitTask;" &&
                method.name == "run"
    }
}

internal val storeRegionInitHook = extensionHook(
    contextRegisterResolver = { "p1" }
) {
    parameters("Landroid/content/Context;")
    custom { method, classDef ->
        classDef.type == "Lcom/ss/android/ugc/aweme/legoImp/task/StoreRegionInitTask;" &&
                method.name == "run"
    }
}
