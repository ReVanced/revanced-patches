package app.revanced.patches.tiktok.misc.extension

import app.revanced.patcher.definingClass
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patches.shared.misc.extension.activityOnCreateExtensionHook
import app.revanced.patches.shared.misc.extension.extensionHook

internal val initHook = activityOnCreateExtensionHook(
    "Lcom/ss/android/ugc/aweme/main/MainActivity;"
)

/**
 * In some cases the extension code can be called before
 * the app main activity onCreate is called.
 *
 * This class is called from startup code titled "BPEA RunnableGuardLancet".
 */
internal val jatoInitHook = extensionHook(
    getContextRegister = { "p1" }
) {
    name("run")
    definingClass("Lcom/ss/android/ugc/aweme/legoImp/task/JatoInitTask;")
    parameterTypes("Landroid/content/Context;")
}

internal val storeRegionInitHook = extensionHook(
    getContextRegister = { "p1" }
) {
    name("run")
    definingClass("Lcom/ss/android/ugc/aweme/legoImp/task/StoreRegionInitTask;")
    parameterTypes("Landroid/content/Context;")
}
