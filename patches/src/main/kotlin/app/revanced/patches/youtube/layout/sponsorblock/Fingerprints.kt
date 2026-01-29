package app.revanced.patches.youtube.layout.sponsorblock

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.youtube.shared.seekbarMethod
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.appendTimeMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Ljava/lang/CharSequence;", "Ljava/lang/CharSequence;", "Ljava/lang/CharSequence;")
    instructions(
        ResourceType.STRING("total_time"),
        method { toString() == "Landroid/content/res/Resources;->getString(I[Ljava/lang/Object;)Ljava/lang/String;" },
        after(Opcode.MOVE_RESULT_OBJECT()),
    )
}

internal val ClassDef.controlsOverlayMethodMatch by ClassDefComposing.composingFirstMethod {
    returnType("V")
    parameterTypes()
    instructions(
        ResourceType.ID.invoke("inset_overlay_view_layout"),
        afterAtMost(20, allOf(Opcode.CHECK_CAST(), type("Landroid/widget/FrameLayout;"))),
    )
}

/**
 * Resolves to the class found in [seekbarMethod].
 */
internal val ClassDef.rectangleFieldInvalidatorMethodMatch by ClassDefComposing.composingFirstMethod {
    returnType("V")
    parameterTypes()
    instructions(method("invalidate"))
}

internal val BytecodePatchContext.adProgressTextViewVisibilityMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Z")
    instructions(
        method {
            name == "setVisibility" && definingClass ==
                "Lcom/google/android/libraries/youtube/ads/player/ui/AdProgressTextView;"
        },
    )
}
