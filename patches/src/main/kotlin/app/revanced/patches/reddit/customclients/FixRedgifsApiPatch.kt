package app.revanced.patches.reddit.customclients

import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.creatingBytecodePatch

const val INSTALL_NEW_CLIENT_METHOD = "install(Lokhttp3/OkHttpClient${'$'}Builder;)Lokhttp3/OkHttpClient;"
const val CREATE_NEW_CLIENT_METHOD = "createClient()Lokhttp3/OkHttpClient;"

fun `Fix Redgifs API`(
    extensionPatch: Patch,
    block: BytecodePatchBuilder.() -> Unit = {},
) = creatingBytecodePatch {
    dependsOn(extensionPatch)

    block()
}
