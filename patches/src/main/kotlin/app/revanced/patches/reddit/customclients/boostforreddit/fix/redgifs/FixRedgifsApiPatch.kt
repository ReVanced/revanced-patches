package app.revanced.patches.reddit.customclients.boostforreddit.fix.redgifs

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patches.reddit.customclients.CREATE_NEW_CLIENT_METHOD
import app.revanced.patches.reddit.customclients.boostforreddit.misc.extension.sharedExtensionPatch
import app.revanced.patches.reddit.customclients.fixRedgifsApiPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/boostforreddit/FixRedgifsApiPatch;"

@Suppress("unused")
val fixRedgifsApi = fixRedgifsApiPatch(
    extensionPatch = sharedExtensionPatch
) {
    compatibleWith("com.rubenmayayo.reddit")

    execute {
        // region Patch Redgifs OkHttp3 client.

        createOkHttpClientFingerprint.method.apply {
            val index = indexOfFirstInstructionOrThrow {
                val reference = getReference<MethodReference>()
                reference?.name == "build" && reference.definingClass == "Lokhttp3/OkHttpClient\$Builder;"
            }
            replaceInstruction(
                index,
                "invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->$CREATE_NEW_CLIENT_METHOD"
            )
        }

        // endregion
    }
}
