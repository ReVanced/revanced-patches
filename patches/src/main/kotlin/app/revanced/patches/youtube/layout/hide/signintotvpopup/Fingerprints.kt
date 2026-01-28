package app.revanced.patches.youtube.layout.hide.signintotvpopup

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patches.shared.misc.mapping.ResourceType

internal val BytecodePatchContext.signInToTvPopupMethod by gettingFirstMutableMethodDeclaratively {
    returnType("Z")
    parameterTypes("Ljava/lang/String;", "Z", "L")
    instructions(ResourceType.STRING("mdx_seamless_tv_sign_in_drawer_fragment_title"))
}
