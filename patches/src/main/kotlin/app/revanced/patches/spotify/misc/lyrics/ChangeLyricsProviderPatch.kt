package app.revanced.patches.spotify.misc.lyrics

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
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

    val lyricsProviderHost by stringOption(
        key = "lyricsProviderHost",
        title = "Lyrics provider host",
        description = "The domain name or IP address of a custom lyrics provider.",
        required = true,
        default = "lyrics.natanchiodi.fr"
    ) {
        // Fix bad data if the user enters a URL (https://whatever.com/path).
        val host = try {
            URI(it!!).host ?: it
        } catch (e: URISyntaxException) {
            return@stringOption false
        }

        // Do a courtesy check if the the host can be resolved.
        // If it does not resolve, then print a warning but use the host anyway.
        // Unresolvable hosts should not be rejected, since the patching environment
        // may not allow network connections or the network may be down.
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

            MutableMethod(method).apply {
                name = "patch_getCustomLyricsProviderHttpClient"
                replaceInstruction(
                    urlAssignmentIndex,
                    "const-string v$urlRegister, \"$lyricsProviderHost\""
                )
                classDef.methods.add(this)
            }
        }

        //endregion

        // Replace the call to the HTTP client builder method used exclusively for lyrics by the modified one.
        fingerprint {
            returns(httpClientBuilderFingerprint.originalMethod.returnType)
            parameters()
            custom { method, _ ->
                method.indexOfFirstInstruction {
                    getReference<MethodReference>() == httpClientBuilderFingerprint.originalMethod
                } >= 0
            }
        }.method.apply {
            val getLyricsHttpClientIndex = indexOfFirstInstructionOrThrow() {
                getReference<MethodReference>() == httpClientBuilderFingerprint.originalMethod
            }

            val getLyricsHttpClientInstruction = getInstruction<BuilderInstruction35c>(getLyricsHttpClientIndex)

            // Call the modified method.
            replaceInstruction(
                getLyricsHttpClientIndex,
                BuilderInstruction35c(
                    Opcode.INVOKE_STATIC,
                    getLyricsHttpClientInstruction.registerCount,
                    getLyricsHttpClientInstruction.registerC,
                    getLyricsHttpClientInstruction.registerD,
                    getLyricsHttpClientInstruction.registerE,
                    getLyricsHttpClientInstruction.registerF,
                    getLyricsHttpClientInstruction.registerG,
                    ImmutableMethodReference(
                        patchedHttpClientBuilderMethod.definingClass,
                        patchedHttpClientBuilderMethod.name, // Only difference to the original method.
                        patchedHttpClientBuilderMethod.parameters,
                        patchedHttpClientBuilderMethod.returnType
                    )
                )
            )
        }

        //endregion
    }
}