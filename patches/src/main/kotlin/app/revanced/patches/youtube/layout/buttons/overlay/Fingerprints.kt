package app.revanced.patches.youtube.layout.buttons.overlay

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.mediaRouteButtonMethod by gettingFirstMethodDeclaratively {
    name("setVisibility")
    definingClass("/MediaRouteButton;")
    parameterTypes("I")
}

internal val BytecodePatchContext.castButtonPlayerFeatureFlagMethodMatch by composingFirstMethod {
    returnType("Z")
    instructions(45690091L())
}

internal val BytecodePatchContext.castButtonActionFeatureFlagMethodMatch by composingFirstMethod {
    returnType("Z")
    instructions(45690090L())
}

internal val BytecodePatchContext.inflateControlsGroupLayoutStubMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes()
    returnType("V")
    instructions(
        ResourceType.ID("youtube_controls_button_group_layout_stub"),
        method("inflate"),
    )
}

internal val BytecodePatchContext.fullscreenButtonMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes("Landroid/view/View;")
    returnType("V")
    instructions(
        ResourceType.ID("fullscreen_button"),
        Opcode.CHECK_CAST()
    )
}

internal val BytecodePatchContext.titleAnchorMethodMatch by composingFirstMethod {
    returnType("V")
    instructions(
        ResourceType.ID("player_collapse_button"),
        Opcode.CHECK_CAST(),
        ResourceType.ID("title_anchor"),
        Opcode.MOVE_RESULT_OBJECT()
    )
}
