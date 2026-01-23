package app.revanced.patches.youtube.layout.buttons.overlay

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.ResourceType.IndexedMatcherPredicateExtension.invoke
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.mediaRouteButtonMethod by gettingFirstMutableMethodDeclaratively {
    name("setVisibility")
    definingClass("/MediaRouteButton;"::endsWith)
    parameterTypes("I")
}

internal val castButtonPlayerFeatureFlagMethodMatch = firstMethodComposite {
    returnType("Z")
    instructions(45690091L())
}

internal val castButtonActionFeatureFlagMethodMatch = firstMethodComposite {
    returnType("Z")
    instructions(45690090L())
}

internal val inflateControlsGroupLayoutStubMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes()
    returnType("V")
    instructions(
        ResourceType.ID("youtube_controls_button_group_layout_stub"),
        method("inflate"),
    )
}
