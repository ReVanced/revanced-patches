package app.revanced.patches.spotify.layout.theme

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.util.*
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/spotify/layout/theme/CustomThemePatch;"

internal val customThemeByteCodePatch = bytecodePatch {
    dependsOn(sharedExtensionPatch)

    val backgroundColor by spotifyBackgroundColor
    val backgroundColorSecondary by spotifyBackgroundColorSecondary

    execute {
        fun MutableMethod.addColorChangeInstructions(literal: Long, colorString: String) {
            val index = indexOfFirstLiteralInstructionOrThrow(literal)
            val register = getInstruction<OneRegisterInstruction>(index).registerA

            addInstructions(
                index + 1,
                """
                    const-string v$register, "$colorString"
                    invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getThemeColor(Ljava/lang/String;)J
                    move-result-wide v$register
                """
            )
        }

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
            // Playlist song list background color.
            addColorChangeInstructions(PLAYLIST_BACKGROUND_COLOR_LITERAL, backgroundColor!!)

            // Share menu background color.
            addColorChangeInstructions(SHARE_MENU_BACKGROUND_COLOR_LITERAL, backgroundColorSecondary!!)
        }

        homeCategoryPillColorsFingerprint.method.apply {
            // Home category pills background color.
            addColorChangeInstructions(HOME_CATEGORY_PILL_COLOR_LITERAL, backgroundColorSecondary!!)
        }

        settingsHeaderColorFingerprint.method.apply {
            // Settings header background color.
            addColorChangeInstructions(SETTINGS_HEADER_COLOR_LITERAL, backgroundColorSecondary!!)
        }
    }
}
