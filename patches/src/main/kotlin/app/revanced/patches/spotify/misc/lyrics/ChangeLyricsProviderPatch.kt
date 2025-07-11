package app.revanced.patches.spotify.misc.lyrics

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
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
    use = false,
) {
    compatibleWith("com.spotify.music")

    val lyricsProviderHost by stringOption(
        key = "lyricsProviderHost",
        default = "lyrics.natanchiodi.fr",
        title = "Lyrics provider host",
        description = "The domain name or IP address of a custom lyrics provider.",
        required = false,
    ) {
        // Fix bad data if the user enters a URL (https://whatever.com/path).
        val host = try {
            URI(it!!).host ?: it
        } catch (e: URISyntaxException) {
            return@stringOption false
        }

        // Do a courtesy check if the host can be resolved.
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
        val httpClientBuilderMethod = httpClientBuilderFingerprint.originalMethod

        // region Create a modified copy of the HTTP client builder method with the custom lyrics provider host.

        val patchedHttpClientBuilderMethod = with(httpClientBuilderMethod) {
            val invokeBuildUrlIndex = indexOfFirstInstructionOrThrow {
                getReference<MethodReference>()?.returnType == "Lokhttp3/HttpUrl;"
            }
            val setUrlBuilderHostIndex = indexOfFirstInstructionReversedOrThrow(invokeBuildUrlIndex) {
                val reference = getReference<MethodReference>()
                reference?.definingClass == "Lokhttp3/HttpUrl${"$"}Builder;" &&
                        reference.parameterTypes.firstOrNull() == "Ljava/lang/String;"
            }
            val hostRegister = getInstruction<FiveRegisterInstruction>(setUrlBuilderHostIndex).registerD

            MutableMethod(this).apply {
                name = "rv_getCustomLyricsProviderHttpClient"
                addInstruction(
                    setUrlBuilderHostIndex,
                    "const-string v$hostRegister, \"$lyricsProviderHost\""
                )

                // Add the patched method to the class.
                httpClientBuilderFingerprint.classDef.methods.add(this)
            }
        }

        //endregion

        // region Replace the call to the HTTP client builder method used exclusively for lyrics by the modified one.

        getLyricsHttpClientFingerprint(httpClientBuilderMethod).method.apply {
            val getLyricsHttpClientIndex = indexOfFirstInstructionOrThrow {
                getReference<MethodReference>() == httpClientBuilderMethod
            }
            val getLyricsHttpClientInstruction = getInstruction<BuilderInstruction35c>(getLyricsHttpClientIndex)

            // Replace the original method call with a call to our patched method.
            replaceInstruction(
                getLyricsHttpClientIndex,
                BuilderInstruction35c(
                    getLyricsHttpClientInstruction.opcode,
                    getLyricsHttpClientInstruction.registerCount,
                    getLyricsHttpClientInstruction.registerC,
                    getLyricsHttpClientInstruction.registerD,
                    getLyricsHttpClientInstruction.registerE,
                    getLyricsHttpClientInstruction.registerF,
                    getLyricsHttpClientInstruction.registerG,
                    ImmutableMethodReference(
                        patchedHttpClientBuilderMethod.definingClass,
                        patchedHttpClientBuilderMethod.name, // Only difference from the original method.
                        patchedHttpClientBuilderMethod.parameters,
                        patchedHttpClientBuilderMethod.returnType
                    )
                )
            )
        }

        //endregion
    }
}
