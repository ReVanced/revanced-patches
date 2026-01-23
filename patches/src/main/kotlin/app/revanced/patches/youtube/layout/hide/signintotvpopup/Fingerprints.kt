package app.revanced.patches.youtube.layout.hide.signintotvpopup

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patches.shared.misc.mapping.ResourceType

internal val BytecodePatchContext.signInToTvPopupMethod by gettingFirstMethodDeclaratively {
    returnType("Z")
    parameterTypes("Ljava/lang/String;", "Z", "L")
    instructions(
        resourceLiteral(
            ResourceType.STRING,
            "mdx_seamless_tv_sign_in_drawer_fragment_title",
        ),
    )
}
