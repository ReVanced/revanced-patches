package app.revanced.patches.twitter.misc.extension

import app.revanced.patches.shared.misc.extension.sharedExtensionPatch
import app.revanced.patches.twitter.misc.extension.hooks.applicationInitHook

val sharedExtensionPatch = sharedExtensionPatch("twitter", applicationInitHook)
