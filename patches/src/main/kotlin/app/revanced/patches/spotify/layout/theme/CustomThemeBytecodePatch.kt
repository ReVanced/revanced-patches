package app.revanced.patches.spotify.layout.theme

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.util.*
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/spotify/layout/theme/CustomThemePatch;"

internal val customThemeByteCodePatch = bytecodePatch {
    dependsOn(sharedExtensionPatch)

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

            encoreColorsClassName = it.originalMethod
                .getInstruction(encoreColorsFieldReferenceIndex)
                .getReference<FieldReference>()!!.definingClass
        }

        val encoreColorLiteral = 0xFF121212

        val encoreColorsConstructorFingerprint = fingerprint {
            accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
            custom { method, classDef ->
                classDef.type == encoreColorsClassName &&
                        method.containsLiteralInstruction(encoreColorLiteral)
            }
        }

        // Playlist song list background color.
        encoreColorsConstructorFingerprint.method.apply {
            val colorResourceIndex = indexOfFirstLiteralInstructionOrThrow(encoreColorLiteral)
            val register = getInstruction<OneRegisterInstruction>(colorResourceIndex).registerA

            addInstructions(
                colorResourceIndex + 1,
                """
                    const-string v$register, "$spotifyBackgroundColor"
                    invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getColorInt(Ljava/lang/String;)J
                    move-result-wide v$register
                """
            )
        }


        val homeCategoryPillColor = 0xFF333333

        val homeCategoryPillColorsFingerprint = fingerprint{
            accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
            custom { method, _ ->
                method.containsLiteralInstruction(0x33000000) && method.containsLiteralInstruction(homeCategoryPillColor)
            }
        }

        // Home category pills background color.
        homeCategoryPillColorsFingerprint.method.apply {
            val colorResourceIndex = indexOfFirstLiteralInstructionOrThrow(homeCategoryPillColor)
            val pillBackgroundColorIndex = indexOfFirstInstructionOrThrow(colorResourceIndex, Opcode.CONST_WIDE)
            val register = getInstruction<OneRegisterInstruction>(pillBackgroundColorIndex).registerA

            addInstructions(
                pillBackgroundColorIndex + 1,
                """
                    const-string v$register, "$spotifyBackgroundColorSecondary"
                    invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getColorInt(Ljava/lang/String;)J
                    move-result-wide v$register
                """
            )
        }
    }
}
