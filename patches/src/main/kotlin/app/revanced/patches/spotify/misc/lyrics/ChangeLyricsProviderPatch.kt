package app.revanced.patches.spotify.misc.lyrics

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import java.net.InetAddress
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
        val host = it!!
            .removePrefix("http://")
            .removePrefix("https://")
            .substringBefore("/")

        try {
            InetAddress.getByName(host)
        } catch (e: UnknownHostException) {
            Logger.getLogger(this::class.java.name).warning(
                "Host $host did not resolve to any domain."
            )
        }
        true
    }

    execute {
        //region Create a patched HTTP client for the requested URL

        val urlAssignmentIndex = clientBuilderFingerprint.stringMatches!!.first().index

        val urlRegister = clientBuilderFingerprint.method.getInstruction<OneRegisterInstruction>(
            urlAssignmentIndex,
        ).registerA

        // Copy the method definition of the HTTP client builder for a valid hostname.
        val patchedClientMethod = clientBuilderFingerprint.method.toMutable().apply {
            name = "getCustomLyricsProviderHttpClient"

            replaceInstruction(
                urlAssignmentIndex,
                "const-string v$urlRegister, \"$lyricsProviderUrl\""
            )
        }

        clientBuilderFingerprint.classDef.methods.add(
            patchedClientMethod
        )

        //endregion

        /**
         * This method is where the HTTP client for lyrics is defined.
         * This patch will replace this HTTP client with a patched HTTP client for the required custom lyrics host.
         */
        val lyricsHttpClientDefinitionFingerprint = fingerprint {
            returns(clientBuilderFingerprint.originalMethod.returnType)
            parameters()
            opcodes(Opcode.CHECK_CAST)
        }

        //region Use the patched HTTP client for lyrics request

        val lyricsFingerprintMethod = lyricsHttpClientDefinitionFingerprint.method

        val getLyricsClientIndex = lyricsFingerprintMethod.indexOfFirstInstructionReversedOrThrow(
            Opcode.INVOKE_STATIC
        )

        val getLyricsClientInstruction = lyricsFingerprintMethod.getInstruction<BuilderInstruction35c>(
            getLyricsClientIndex
        )

        /**
         * Adjust the lyrics HTTP builder method name to the method defined above.
         * In this way the copied method is called rather than the stock one, returning the patched HTTP builder.
         */
        lyricsFingerprintMethod.replaceInstruction(
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
                    patchedClientMethod.definingClass,
                    patchedClientMethod.name, // This is the only difference to the original method.
                    patchedClientMethod.parameters,
                    patchedClientMethod.returnType
                )
            )
        )

        //endregion
    }
}