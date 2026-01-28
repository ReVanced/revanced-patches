package app.revanced.patches.youtube.layout.hide.relatedvideooverlay

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.relatedEndScreenResultsParentMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    instructions(
        ResourceType.LAYOUT("app_related_endscreen_results"),
    )
}

context(_: BytecodePatchContext)
internal fun ClassDef.getRelatedEndScreenResultsMethod() = firstMutableMethodDeclaratively {
    returnType("V")
    parameterTypes(
        "I",
        "Z",
        "I",
    )
}
