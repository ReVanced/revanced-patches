package app.revanced.patches.reddit.customclients

import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.bytecodePatch

const val RESOLVE_S_LINK_METHOD = "patchResolveSLink(Ljava/lang/String;)Z"
const val SET_ACCESS_TOKEN_METHOD = "patchSetAccessToken(Ljava/lang/String;)V"

fun fixSLinksPatch(
    extensionPatch: Patch<*>,
    block: BytecodePatchBuilder.() -> Unit = {},
) = bytecodePatch(name = "Fix /s/ links") {
    dependsOn(extensionPatch)

    block()
}
