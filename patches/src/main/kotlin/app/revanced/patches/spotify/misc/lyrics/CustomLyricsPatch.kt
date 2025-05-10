package app.revanced.patches.spotify.misc.lyrics

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21t
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference


@Suppress("unused")
val customLyricsPatch = bytecodePatch(
    name = "Custom lyrics provider",
    description = "Make Spotify use a custom lyrics provider.",
) {
    compatibleWith("com.spotify.music")

    val lyricsProviderUrlOption = stringOption(
        key = "lyricsProviderUrl",
        title = "Lyrics provider URL",
        description = "The custom lyrics provider URL.",
        required = true,
    )

    execute {
        val lyricsProviderUrl by lyricsProviderUrlOption()
        var patchedClientMethod: ImmutableMethod?

        fun addLyricsProviderToValidHosts(fingerprint: Fingerprint) {
            with(fingerprint) {
                val validUrlAssignmentIndex = method.indexOfFirstInstructionOrThrow(
                    stringMatches!!.first().index, Opcode.CONST_STRING
                )

                val urlComparisonIndex = method.indexOfFirstInstructionOrThrow(
                    validUrlAssignmentIndex,
                    Opcode.INVOKE_VIRTUAL
                )

                val ifValidUrlIndex = method.indexOfFirstInstructionOrThrow(
                    urlComparisonIndex,
                    Opcode.IF_NEZ
                )

                val label = method.getInstruction<BuilderInstruction21t>(ifValidUrlIndex).target

                val (stringRegister, compareToRegister) = method.getInstruction<FiveRegisterInstruction>(urlComparisonIndex).let { it.registerC to it.registerD }

                method.addInstructions(ifValidUrlIndex + 1,
                    """
                    const-string v$stringRegister, "$lyricsProviderUrl"
                    
                    invoke-virtual {v$compareToRegister, v$stringRegister}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z
                    
                    move-result v$stringRegister
                """
                )

                method.addInstruction(ifValidUrlIndex + 1 + 3,
                    BuilderInstruction21t(Opcode.IF_NEZ, stringRegister, label))
            }
        }

        addLyricsProviderToValidHosts(oauthHostnameCheckFingerprint)
        addLyricsProviderToValidHosts(webgateHostnameCheckFingerprint)

        val clientBuilderMethodName = "getCustomLyricsProviderClient"

        with(clientBuilderFingerprint) {
            val clientStringAssignmentIndex = method.indexOfFirstInstructionOrThrow(
                stringMatches!!.first().index,
                Opcode.CONST_STRING,
            )

            val urlRegister = method.getInstruction<OneRegisterInstruction>(clientStringAssignmentIndex).registerA

            patchedClientMethod = ImmutableMethod(
                method.definingClass,
                clientBuilderMethodName,
                method.parameters,
                method.returnType,
                method.accessFlags,
                null,
                null,
                MutableMethodImplementation(6),
            )

            classDef.methods.add(
                patchedClientMethod!!.toMutable().apply {
                    addInstructions(method.instructions)

                    replaceInstruction(clientStringAssignmentIndex, "const-string v$urlRegister, \"$lyricsProviderUrl\"")
                }
            )
        }

        with(executeFingerprint.method) {
            val getLyricsClientIndex = indexOfFirstInstructionReversedOrThrow(Opcode.INVOKE_STATIC)
            val getLyricsClientInstruction = getInstruction<BuilderInstruction35c>(getLyricsClientIndex)

            replaceInstruction(getLyricsClientIndex,
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
                        clientBuilderMethodName,
                        patchedClientMethod!!.parameters,
                        patchedClientMethod!!.returnType
                    )
                )
            )
        }
    }
}