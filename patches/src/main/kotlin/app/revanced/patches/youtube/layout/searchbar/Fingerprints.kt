package app.revanced.patches.youtube.layout.searchbar

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstImmutableMethodDeclaratively
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.youtube.layout.hide.general.yoodlesImageViewMethod
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.setWordmarkHeaderMethod by gettingFirstImmutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Landroid/widget/ImageView;")
    instructions(
        ResourceType.ATTR("ytPremiumWordmarkHeader"),
        ResourceType.ATTR("ytWordmarkHeader"),
    )
}

/**
 * Matches the same method as [yoodlesImageViewMethod].
 */
internal val BytecodePatchContext.wideSearchbarLayoutMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Landroid/view/View;")
    parameterTypes("L", "L")
    instructions(
        ResourceType.LAYOUT("action_bar_ringo"),
    )
}
