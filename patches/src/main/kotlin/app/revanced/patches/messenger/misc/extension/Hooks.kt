package app.revanced.patches.messenger.misc.extension

import app.revanced.patches.shared.misc.extension.extensionHook

internal val messengerApplicationOnCreateHook = extensionHook {
    custom { method, classDef ->
        method.name == "onCreate" && classDef.endsWith("/MessengerApplication;")
    }
}
