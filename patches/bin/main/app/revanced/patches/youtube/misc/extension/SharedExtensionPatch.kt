package app.revanced.patches.youtube.misc.extension

import app.revanced.patches.shared.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.extension.hooks.*

val sharedExtensionPatch = sharedExtensionPatch("youtube", applicationInitHook)
