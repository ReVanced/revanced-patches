package app.revanced.patches.reddit.customclients.baconreader.fix.redgifs

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.removeInstructions
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patches.reddit.customclients.fixRedgifsApi
import app.revanced.patches.reddit.customclients.INSTALL_NEW_CLIENT_METHOD
import app.revanced.patches.reddit.customclients.baconreader.misc.extension.sharedExtensionPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/baconreader/FixRedgifsApiPatch;"

@Suppress("unused")
val fixRedgifsApi = fixRedgifsApi(
    extensionPatch = sharedExtensionPatch
) {
    compatibleWith(
        "com.onelouder.baconreader",
        "com.onelouder.baconreader.premium",
    )

    apply {
        // region Patch Redgifs OkHttp3 client.

        // Remove conflicting OkHttp interceptors.
        val originalInterceptorInstallIndex = getOkHttpClientMethod.indexOfFirstInstructionOrThrow {
            opcode == Opcode.NEW_INSTANCE && getReference<TypeReference>()?.type == "Lcom/onelouder/baconreader/media/gfycat/RedGifsManager\$HeaderInterceptor;"
        }
        getOkHttpClientMethod.removeInstructions(originalInterceptorInstallIndex, 5)

        val index = getOkHttpClientMethod.indexOfFirstInstructionOrThrow {
            val reference = getReference<MethodReference>()
            reference?.name == "build" && reference.definingClass == "Lokhttp3/OkHttpClient\$Builder;"
        }
        val register = getOkHttpClientMethod.getInstruction<FiveRegisterInstruction>(index).registerC
        getOkHttpClientMethod.replaceInstruction(
            index,
            """
            invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->$INSTALL_NEW_CLIENT_METHOD
            """
        )
    }

    // endregion

}
