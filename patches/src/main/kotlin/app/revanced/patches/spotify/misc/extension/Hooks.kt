package app.revanced.patches.spotify.misc.extension

import app.revanced.patches.shared.misc.extension.extensionHook
import app.revanced.patches.spotify.shared.mainActivityOnCreateFingerprint

internal val mainActivityOnCreateHook = extensionHook { mainActivityOnCreateFingerprint }
