package app.revanced.patches.spotify.misc.lyrics

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
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


@Suppress("unused")
val customLyricsPatch = bytecodePatch(
    name = "Change lyrics provider",
    description = "Changes the lyrics provider to a custom one.",
) {
    compatibleWith("com.spotify.music")

    var lyricsUrlHost by stringOption(
        key = "lyricsProviderUrl",
        title = "Lyrics provider URL/hostname",
        description = "The URL or the hostname to a custom lyrics provider, " +
                "such as SpotifyMobileLyricsAPI: https://github.com/Natoune/SpotifyMobileLyricsAPI.",
        required = true
    )

    execute {
        lyricsUrlHost = lyricsUrlHost!!
            .removePrefix("http://")
            .removePrefix("https://")
            .substringBefore("/")

        var patchedClientMethod: ImmutableMethod?

        clientBuilderFingerprint.method.apply {
            /**
             * Copy the method definition of the HTTP client builder for a valid hostname.
             */
            patchedClientMethod = ImmutableMethod(
                definingClass,
                "getCustomLyricsProviderClient",
                parameters,
                returnType,
                accessFlags,
                null,
                null,
                MutableMethodImplementation(6),
            )

            val urlRegister = getInstruction<OneRegisterInstruction>(
                clientBuilderFingerprint.stringMatches!!.first().index,
            ).registerA

            /**
             * Copy the instructions into the new method and replace the assigned hostname with our host.
             */
            clientBuilderFingerprint.classDef.methods.add(
                patchedClientMethod!!.toMutable().apply {
                    addInstructions(clientBuilderFingerprint.method.instructions)

                    replaceInstruction(
                        clientBuilderFingerprint.stringMatches!!.first().index,
                        "const-string v$urlRegister, \"$lyricsUrlHost\"")
                }
            )
        }

        /**
         * Adjust the lyrics HTTP builder method name to the method defined above.
         * In this way the copied method is called rather than the stock one, returning the patched HTTP builder.
         */
        executeFingerprint.method.apply {
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