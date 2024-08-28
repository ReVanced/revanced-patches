package app.revanced.patches.googlenews.misc.extension

import app.revanced.patches.googlenews.misc.extension.hooks.startActivityInitHook
import app.revanced.patches.shared.misc.extensions.sharedExtensionPatch

val extensionPatch = sharedExtensionPatch(startActivityInitHook)
