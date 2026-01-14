package app.revanced.patches.reddit.customclients.boostforreddit.fix.redgifs

import app.revanced.patcher.extensions.methodReference
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patches.reddit.customclients.CREATE_NEW_CLIENT_METHOD
import app.revanced.patches.reddit.customclients.fixRedgifsApi
import app.revanced.patches.reddit.customclients.boostforreddit.misc.extension.sharedExtensionPatch
import app.revanced.util.indexOfFirstInstructionOrThrow

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/boostforreddit/FixRedgifsApiPatch;"

@Suppress("unused")
val fixRedgifsApi = fixRedgifsApi(
    extensionPatch = sharedExtensionPatch
) {
    compatibleWith("com.rubenmayayo.reddit")

    apply {
        // region Patch Redgifs OkHttp3 client.

        val index = createOkHttpClientMethod.indexOfFirstInstructionOrThrow {
            val reference = methodReference
            reference?.name == "build" && reference.definingClass == "Lokhttp3/OkHttpClient\$Builder;"
        }
        createOkHttpClientMethod.replaceInstruction(
            index,
            "invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->$CREATE_NEW_CLIENT_METHOD"
        )

        // endregion
    }
}
