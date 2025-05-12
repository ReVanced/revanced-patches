package app.revanced.patches.spotify.misc.lyrics

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.logging.Logger


@Suppress("unused")
val customLyricsPatch = bytecodePatch(
    name = "Change lyrics provider",
    description = "Changes the lyrics provider to a custom one.",
) {
    compatibleWith("com.spotify.music")

    val lyricsUrlHost by stringOption(
        key = "lyricsProviderUrl",
        title = "Lyrics provider URL/hostname",
        description = "The URL or the hostname to a custom lyrics provider.",
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
        var patchedClientMethod: ImmutableMethod?

        clientBuilderFingerprint.apply {
            /**
             * Copy the method definition of the HTTP client builder for a valid hostname.
             */
            patchedClientMethod = ImmutableMethod(
                method.definingClass,
                "getCustomLyricsProviderClient",
                method.parameters,
                method.returnType,
                method.accessFlags,
                null,
                null,
                MutableMethodImplementation(6),
            )

            val urlRegister = method.getInstruction<OneRegisterInstruction>(
                stringMatches!!.first().index,
            ).registerA

            /**
             * Copy the instructions into the new method and replace the assigned hostname with our host.
             */
            classDef.methods.add(
                patchedClientMethod!!.toMutable().apply {
                    addInstructions(method.instructions)

                    replaceInstruction(
                        stringMatches!!.first().index,
                        "const-string v$urlRegister, \"$lyricsUrlHost\"")
                }
            )
        }

        /**
         * This method is where the HTTP client for lyrics is defined.
         * This patch will replace this HTTP client with a patched HTTP client for the required custom lyrics host.
         */
        val lyricsHttpClientDefinitionFingerprint = fingerprint {
            returns(clientBuilderFingerprint.originalMethod.returnType)
            parameters()
            opcodes(Opcode.CHECK_CAST)
        }

        /**
         * Adjust the lyrics HTTP builder method name to the method defined above.
         * In this way the copied method is called rather than the stock one, returning the patched HTTP builder.
         */
        lyricsHttpClientDefinitionFingerprint.method.apply {
            val getLyricsClientIndex = indexOfFirstInstructionReversedOrThrow(
                Opcode.INVOKE_STATIC
            )

            val getLyricsClientInstruction = getInstruction<BuilderInstruction35c>(
                getLyricsClientIndex
            )

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
                        patchedClientMethod!!.definingClass,
                        patchedClientMethod!!.name, //This is the only thing that will be different.
                        patchedClientMethod!!.parameters,
                        patchedClientMethod!!.returnType
                    )
                )
            )
        }
    }
}