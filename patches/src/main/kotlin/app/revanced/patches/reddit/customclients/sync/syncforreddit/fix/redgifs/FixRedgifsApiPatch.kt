package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.redgifs

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patches.reddit.customclients.INSTALL_NEW_CLIENT_METHOD
import app.revanced.patches.reddit.customclients.fixRedgifsApiPatch
import app.revanced.patches.reddit.customclients.sync.syncforreddit.extension.sharedExtensionPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/syncforreddit/FixRedgifsApiPatch;"

@Suppress("unused")
val fixRedgifsApi = fixRedgifsApiPatch(
    extensionPatch = sharedExtensionPatch
) {
    compatibleWith(
        "com.laurencedawson.reddit_sync",
        "com.laurencedawson.reddit_sync.pro",
        "com.laurencedawson.reddit_sync.dev",
    )

    execute {
        // region Patch Redgifs OkHttp3 client.

        createOkHttpClientFingerprint.method.apply {
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

        getDefaultUserAgentFingerprint.method.apply {
            addInstructions(
                0,
                """
                invoke-static { }, ${getOriginalUserAgentFingerprint.method}
                move-result-object v0
                return-object v0
                """
            )
        }

        // endregion
    }
}
