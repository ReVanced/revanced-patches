package app.revanced.patches.messenger.misc.extension

import app.revanced.patches.shared.misc.extension.extensionHook

internal val mainActivityOnCreateHook = extensionHook {
    strings("MainActivity_onCreate_begin")
}
