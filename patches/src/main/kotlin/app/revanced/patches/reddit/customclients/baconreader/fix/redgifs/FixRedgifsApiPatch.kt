package app.revanced.patches.reddit.customclients.baconreader.fix.redgifs

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patches.reddit.customclients.INSTALL_NEW_CLIENT_METHOD
import app.revanced.patches.reddit.customclients.baconreader.misc.extension.sharedExtensionPatch
import app.revanced.patches.reddit.customclients.fixRedgifsApiPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/baconreader/FixRedgifsApiPatch;"

@Suppress("unused")
val fixRedgifsApi = fixRedgifsApiPatch(
    extensionPatch = sharedExtensionPatch
) {
    compatibleWith(
        "com.onelouder.baconreader",
        "com.onelouder.baconreader.premium",
    )

    execute {
        // region Patch Redgifs OkHttp3 client.

        getOkHttpClientFingerprint.method.apply {
            // Remove conflicting OkHttp interceptors.
            val originalInterceptorInstallIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.NEW_INSTANCE && getReference<TypeReference>()?.type == "Lcom/onelouder/baconreader/media/gfycat/RedGifsManager\$HeaderInterceptor;"
            }
            removeInstructions(originalInterceptorInstallIndex, 5)

            val index = indexOfFirstInstructionOrThrow {
                val reference = getReference<MethodReference>()
                reference?.name == "build" && reference.definingClass == "Lokhttp3/OkHttpClient\$Builder;"
            }
            val register = getInstruction<FiveRegisterInstruction>(index).registerC
            replaceInstruction(
                index,
                """
                invoke-static       { v$register }, $EXTENSION_CLASS_DESCRIPTOR->$INSTALL_NEW_CLIENT_METHOD
                """
            )
        }

        // endregion
    }
}
