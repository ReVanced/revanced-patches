package app.revanced.patches.youtube.interaction.doubletap

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/DisableDoubleTapActionsPatch;"

@Suppress("unused")
val disableDoubleTapActionsPatch = bytecodePatch(
    name = "Disable double tap actions",
    description = "Adds an option to disable player double tap gestures.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "20.07.39",
            "20.12.46",
            "20.13.41",
        )
    )

    execute {
        addResources("youtube", "interaction.doubletap.disableDoubleTapActionsPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_disable_chapter_skip_double_tap"),
        )

        val doubleTapInfoGetSeekSourceFingerprint = fingerprint {
            accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
            parameters("Z")
            returns(seekTypeEnumFingerprint.originalClassDef.type)
            opcodes(
                Opcode.IF_EQZ,
                Opcode.SGET_OBJECT,
                Opcode.RETURN_OBJECT,
                Opcode.SGET_OBJECT,
                Opcode.RETURN_OBJECT,
            )
            custom { _, classDef ->
                classDef.fields.count() == 4
            }
        }

        // Force isChapterSeek flag to false.
        doubleTapInfoGetSeekSourceFingerprint.method.addInstructions(
            0,
            """
                invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->disableDoubleTapChapters(Z)Z
                move-result p1
            """
        )

        doubleTapInfoCtorFingerprint.match(
            doubleTapInfoGetSeekSourceFingerprint.classDef
        ).method.addInstructions(
            0,
            """
                invoke-static { p3 }, $EXTENSION_CLASS_DESCRIPTOR->disableDoubleTapChapters(Z)Z
                move-result p3
            """
        )
    }
}

@Deprecated("Patch was renamed", ReplaceWith("disableDoubleTapActionsPatch"))
val disableChapterSkipDoubleTapPatch = bytecodePatch {
    dependsOn(disableDoubleTapActionsPatch)
}
