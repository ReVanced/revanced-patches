package app.revanced.patches.music.misc.extension

import app.revanced.patches.music.misc.extension.hooks.applicationInitHook
import app.revanced.patches.shared.misc.extension.sharedExtensionPatch

val sharedExtensionPatch = sharedExtensionPatch(applicationInitHook)
