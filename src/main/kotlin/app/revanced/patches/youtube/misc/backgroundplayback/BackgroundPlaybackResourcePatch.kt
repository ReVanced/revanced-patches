package app.revanced.patches.youtube.misc.backgroundplayback

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings

internal var prefBackgroundAndOfflineCategoryId = -1L
    private set

internal val backgroundPlaybackResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    execute {
        prefBackgroundAndOfflineCategoryId = resourceMappings["string", "pref_background_and_offline_category"]
    }
}
