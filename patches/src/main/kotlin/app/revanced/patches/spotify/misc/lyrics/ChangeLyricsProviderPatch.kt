package app.revanced.patches.spotify.misc.lyrics

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.spotify.misc.extension.IS_SPOTIFY_LEGACY_APP_TARGET
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import java.net.InetAddress
import java.net.URI
import java.net.URISyntaxException
import java.net.UnknownHostException
import java.util.logging.Logger


@Suppress("unused")
val changeLyricsProviderPatch = bytecodePatch(
    name = "Change lyrics provider",
    description = "Changes the lyrics provider to a custom one.",
) {
    compatibleWith("com.spotify.music")

    val lyricsProviderUrl by stringOption(
        key = "lyricsProviderUrl",
        title = "Lyrics provider URL",
        description = "The URL to a custom lyrics provider.",
        required = true,
        default = "lyrics.natanchiodi.fr"
    ) {
        val host = try {
            URI(it!!).host ?: it
        } catch (e: URISyntaxException) {
            return@stringOption false
        }

        try {
            InetAddress.getByName(host)
        } catch (e: UnknownHostException) {
            Logger.getLogger(this::class.java.name).warning(
                "Host \"$host\" did not resolve to any domain."
            )
        }
        true
    }

    execute {
        if(IS_SPOTIFY_LEGACY_APP_TARGET) {
            Logger.getLogger(this::class.java.name).severe(
                "Change lyrics provider patch is not supported for this target version."
            )
            return@execute
        }

        // region Create a modified copy of the HTTP client builder method with the custom lyrics provider URL.

        val patchedHttpClientBuilderMethod = with(httpClientBuilderFingerprint) {
            val urlAssignmentIndex = stringMatches!!.first().index
            val urlRegister = method.getInstruction<OneRegisterInstruction>(
                urlAssignmentIndex,
            ).registerA

            method.toMutable().apply {
                name = "patch_getCustomLyricsProviderHttpClient"
                replaceInstruction(
                    urlAssignmentIndex,
                    "const-string v$urlRegister, \"$lyricsProviderUrl\""
                )
                classDef.methods.add(this)
            }
        }

        //endregion

        // Replace the call to the HTTP client builder method used exclusively for lyrics by the modified one.
        val lyricsHttpClientDefinitionFingerprint = fingerprint {
            returns(httpClientBuilderFingerprint.originalMethod.returnType)
            parameters()
            custom { method, _ ->
                method.indexOfFirstInstruction {
                    getReference<MethodReference>() == httpClientBuilderFingerprint.originalMethod
                } >= 0
            }
        }

        // region Conditionally use the modified HTTP client builder method when the client is used for the lyrics API.

        lyricsHttpClientDefinitionFingerprint.method.apply {
            val getLyricsClientIndex = indexOfFirstInstructionOrThrow() {
                getReference<MethodReference>() == httpClientBuilderFingerprint.originalMethod
            }

            val getLyricsClientInstruction = getInstruction<BuilderInstruction35c>(
                getLyricsClientIndex
            )

            /**
             * Adjust the lyrics HTTP builder method name to the method defined above.
             * In this way the copied method is called rather than the stock one, returning the patched HTTP builder.
             */
            replaceInstruction(
                getLyricsClientIndex,
                BuilderInstruction35c(
                    Opcode.INVOKE_STATIC,
                    getLyricsClientInstruction.registerCount,
                    getLyricsClientInstruction.registerC,
                    getLyricsClientInstruction.registerD,
                    getLyricsClientInstruction.registerE,
                    getLyricsClientInstruction.registerF,
                    getLyricsClientInstruction.registerG,
                    ImmutableMethodReference(
                        patchedHttpClientBuilderMethod.definingClass,
                        patchedHttpClientBuilderMethod.name, // This is the only difference to the original method.
                        patchedHttpClientBuilderMethod.parameters,
                        patchedHttpClientBuilderMethod.returnType
                    )
                )
            )
        }

        //endregion
    }
}