package app.revanced.patches.googlenews.misc.extension

import app.revanced.patches.googlenews.misc.extension.hooks.startActivityInitHook
import app.revanced.patches.shared.misc.extension.sharedExtensionPatch

val extensionPatch = sharedExtensionPatch(startActivityInitHook)
