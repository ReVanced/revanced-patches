package app.revanced.patches.reddit.customclients.baconreader.misc.extension

import app.revanced.patches.reddit.customclients.baconreader.misc.extension.hooks.initHook
import app.revanced.patches.shared.misc.extension.sharedExtensionPatch

val sharedExtensionPatch = sharedExtensionPatch("baconreader", initHook)
