package app.revanced.patches.spotify.layout.theme

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

val customThemeByteCodePatch = bytecodePatch {
    extendWith("extensions/spotify.rve")

    execute {
        // Playlist song list background color
        val encoreColorsConstructorFingerprint = fingerprint {
            val encoreThemeFingerprint = fingerprint {
                strings("Encore theme was not provided. Please wrap your content with ProvideEncoreTheme. For @Previews use com.spotify.encore.tooling.preview.EncorePreview()")
            }

            val encoreColorsFieldReferenceIndex = encoreThemeFingerprint.stringMatches!!.first().index + 5

            val encoreColorsClassName = encoreThemeFingerprint.originalMethod
                .getInstruction(encoreColorsFieldReferenceIndex)
                .getReference<FieldReference>()!!.definingClass

            accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
            custom { _, c -> c.type == encoreColorsClassName }
        }

        val playlistSongListBackgroundColorIndex = 2

        encoreColorsConstructorFingerprint.method.addInstructions(
            playlistSongListBackgroundColorIndex,
            """
                const-string v2, "$backgroundColor"
                invoke-static {v2}, Lapp/revanced/extension/spotify/layout/theme/CustomThemePatch;->getColorInt(Ljava/lang/String;)J
                move-result-wide v2
            """
        )
    }
}