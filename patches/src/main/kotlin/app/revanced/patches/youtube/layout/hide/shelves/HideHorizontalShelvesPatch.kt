package app.revanced.patches.youtube.layout.hide.shelves

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.engagement.engagementPanelHookPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.litho.observer.layoutReloadObserverPatch
import app.revanced.patches.youtube.misc.navigation.navigationBarHookPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch

private const val FILTER_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/litho/HorizontalShelvesFilter;"

internal val hideHorizontalShelvesPatch = bytecodePatch {
    dependsOn(
        sharedExtensionPatch,
        lithoFilterPatch,
        playerTypeHookPatch,
        navigationBarHookPatch,
        engagementPanelHookPatch,
        layoutReloadObserverPatch,
    )

    apply {
        addLithoFilter(FILTER_CLASS_DESCRIPTOR)
    }
}
