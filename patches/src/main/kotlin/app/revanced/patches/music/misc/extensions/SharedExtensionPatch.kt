package app.revanced.patches.music.misc.extensions

import app.revanced.patches.music.misc.extensions.hooks.applicationInitHook
import app.revanced.patches.shared.misc.extensions.sharedExtensionPatch

val sharedExtensionPatch = sharedExtensionPatch(applicationInitHook)
