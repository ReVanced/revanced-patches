package app.revanced.patches.youtube.ad.general

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.util.containsLiteralInstruction
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionReversed
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val BytecodePatchContext.fullScreenEngagementAdContainerMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes()
    custom {
        containsLiteralInstruction(fullScreenEngagementAdContainer) &&
                indexOfAddListInstruction(this) >= 0
    }
}

internal fun indexOfAddListInstruction(method: Method) = method.indexOfFirstInstructionReversed {
    getReference<MethodReference>()?.name == "add"
}


internal val BytecodePatchContext.getPremiumViewMethodMatch by composingFirstMethod {
    name("onMeasure")
    definingClass("Lcom/google/android/apps/youtube/app/red/presenter/CompactYpcOfferModuleView;")
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("I", "I")
    opcodes(
        Opcode.ADD_INT_2ADDR,
        Opcode.ADD_INT_2ADDR,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID
    )
}

internal val BytecodePatchContext.lithoDialogBuilderMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("[B", "L")
    instructions(
        allOf(Opcode.INVOKE_VIRTUAL(), method("show")),
        ResourceType.STYLE("SlidingDialogAnimation")
    )
}


internal val BytecodePatchContext.playerOverlayTimelyShelfMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Ljava/lang/Object;")
    instructions(
        "player_overlay_timely_shelf"(),
        "innertube_cue_range"(),
        "Null id"(),
        "Null onExitActions"()
    )
}

