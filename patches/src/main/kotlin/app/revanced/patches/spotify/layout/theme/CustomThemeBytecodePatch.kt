package app.revanced.patches.spotify.layout.theme

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.containsLiteralInstruction
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstLiteralInstructionOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/spotify/layout/theme/CustomThemePatch;"

internal val customThemeByteCodePatch = bytecodePatch {
    extendWith("extensions/spotify.rve")

    execute {
        val encoreColorsClassName : String

        encoreThemeFingerprint.let {
            // Find index of the first static get found after the string constant.
            val encoreColorsFieldReferenceIndex = encoreThemeFingerprint.let {
                it.method.indexOfFirstInstructionOrThrow(
                    it.stringMatches!!.first().index,
                    Opcode.SGET_OBJECT
                )
            }

            // Playlist song list background color.
            encoreColorsClassName = it.originalMethod
                .getInstruction(encoreColorsFieldReferenceIndex)
                .getReference<FieldReference>()!!.definingClass
        }

        val encoreColorLiteral = 0xff121212L

        val encoreColorsConstructorFingerprint = fingerprint {
            accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
            custom { method, classDef ->
                classDef.type == encoreColorsClassName &&
                        method.containsLiteralInstruction(encoreColorLiteral)
            }
        }

        encoreColorsConstructorFingerprint.method.apply {
            val colorResourceIndex = indexOfFirstLiteralInstructionOrThrow(encoreColorLiteral)
            val register = getInstruction<OneRegisterInstruction>(colorResourceIndex).registerA

            addInstructions(
                colorResourceIndex,
                """
                    const-string v$register, "$spotifyBackgroundColor"
                    invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getColorInt(Ljava/lang/String;)J
                    move-result-wide v$register
                """
            )
        }
    }
}