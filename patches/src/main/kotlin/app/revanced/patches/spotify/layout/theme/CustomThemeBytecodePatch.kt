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
        val encoreColorsClassName = with(encoreThemeFingerprint) {
            // Find index of the first static get found after the string constant.
            val encoreColorsFieldReferenceIndex = originalMethod.indexOfFirstInstructionOrThrow(
                stringMatches!!.first().index,
                Opcode.SGET_OBJECT
            )

            originalMethod.getInstruction(encoreColorsFieldReferenceIndex)
                .getReference<FieldReference>()!!.definingClass
        }

        val encoreColorsConstructorFingerprint = fingerprint {
            accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
            custom { method, classDef ->
                classDef.type == encoreColorsClassName &&
                        method.containsLiteralInstruction(PLAYLIST_BACKGROUND_COLOR_LITERAL)
            }
        }

        encoreColorsConstructorFingerprint.method.apply {
            val songListBackgroundColorInstructionIndex = indexOfFirstLiteralInstructionOrThrow(PLAYLIST_BACKGROUND_COLOR_LITERAL)
            val songListBackgroundColorRegister = getInstruction<OneRegisterInstruction>(songListBackgroundColorInstructionIndex).registerA

            // Playlist song list background color.
            addInstructions(
                songListBackgroundColorInstructionIndex + 1,
                """
                    const-string v$songListBackgroundColorRegister, "$spotifyBackgroundColor"
                    invoke-static { v$songListBackgroundColorRegister }, $EXTENSION_CLASS_DESCRIPTOR->getColorLong(Ljava/lang/String;)J
                    move-result-wide v$songListBackgroundColorRegister
                """
            )

            val shareMenuBackgroundColorInstructionIndex = indexOfFirstLiteralInstructionOrThrow(SHARE_MENU_BACKGROUND_COLOR_LITERAL)
            val shareMenuBackgroundColorRegister = getInstruction<OneRegisterInstruction>(shareMenuBackgroundColorInstructionIndex).registerA

            // Share menu background color.
            addInstructions(
                shareMenuBackgroundColorInstructionIndex + 1,
                """
                    const-string v$shareMenuBackgroundColorRegister, "$spotifyBackgroundColorSecondary"
                    invoke-static { v$shareMenuBackgroundColorRegister }, $EXTENSION_CLASS_DESCRIPTOR->getColorLong(Ljava/lang/String;)J
                    move-result-wide v$shareMenuBackgroundColorRegister
                """
            )
        }

        // Home category pills background color.
        homeCategoryPillColorsFingerprint.method.apply {
            val pillBackgroundColorInstructionIndex = indexOfFirstLiteralInstructionOrThrow(HOME_CATEGORY_PILL_COLOR_LITERAL)
            val register = getInstruction<OneRegisterInstruction>(pillBackgroundColorInstructionIndex).registerA

            addInstructions(
                pillBackgroundColorInstructionIndex + 1,
                """
                    const-string v$register, "$spotifyBackgroundColorSecondary"
                    invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getColorLong(Ljava/lang/String;)J
                    move-result-wide v$register
                """
            )
        }

        // Settings header background color.
        settingsHeaderColorFingerprint.method.apply {
            val headerBackgroundColorInstructionIndex = indexOfFirstLiteralInstructionOrThrow(SETTINGS_HEADER_COLOR_LITERAL)
            val register = getInstruction<OneRegisterInstruction>(headerBackgroundColorInstructionIndex).registerA

            addInstructions(
                headerBackgroundColorInstructionIndex + 1,
                """
                    const-string v$register, "$spotifyBackgroundColorSecondary"
                    invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getColorInt(Ljava/lang/String;)J
                    move-result-wide v$register
                """
            )
        }
    }
}
