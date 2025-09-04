package app.revanced.patches.reddit.customclients

import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.bytecodePatch

const val INSTALL_NEW_CLIENT_METHOD = "install(Lokhttp3/OkHttpClient${'$'}Builder;)Lokhttp3/OkHttpClient;"
const val CREATE_NEW_CLIENT_METHOD = "createClient()Lokhttp3/OkHttpClient;"

fun fixRedgifsApiPatch(
    extensionPatch: Patch<*>,
    block: BytecodePatchBuilder.() -> Unit = {},
) = bytecodePatch(name = "Fix Redgifs API") {
    dependsOn(extensionPatch)

    block()
}
