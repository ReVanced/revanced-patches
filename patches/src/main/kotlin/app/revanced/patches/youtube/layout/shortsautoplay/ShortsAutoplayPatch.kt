package app.revanced.patches.youtube.layout.shortsautoplay

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_34_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.mainActivityOnCreateFingerprint
import app.revanced.util.findInstructionIndicesReversedOrThrow
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/ShortsAutoplayPatch;"

val shortsAutoplayPatch = bytecodePatch(
    name = "Shorts autoplay",
    description = "Adds options to automatically play the next Short.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        resourceMappingPatch,
        versionCheckPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.45.38",
            "19.46.42",
            "19.47.53",
        ),
    )

    execute {
        addResources("youtube", "layout.shortsautoplay.shortsAutoplayPatch")

        PreferenceScreen.SHORTS.addPreferences(
            SwitchPreference("revanced_shorts_autoplay"),
        )

        if (is_19_34_or_greater) {
            PreferenceScreen.SHORTS.addPreferences(
                SwitchPreference("revanced_shorts_autoplay_background"),
            )
        }

        // Main activity is used to check if app is in pip mode.
        mainActivityOnCreateFingerprint.method.addInstruction(
            1,
            "invoke-static/range { p0 .. p0 }, $EXTENSION_CLASS_DESCRIPTOR->" +
                "setMainActivity(Landroid/app/Activity;)V",
        )

        val reelEnumClass = reelEnumConstructorFingerprint.originalClassDef.type

        reelEnumConstructorFingerprint.method.apply {
            val insertIndex = reelEnumConstructorFingerprint.filterMatches.first().index

            addInstructions(
                insertIndex,
                """
                    # Pass the first enum value to extension.
                    # Any enum value of this type will work.
                    sget-object v0, $reelEnumClass->a:$reelEnumClass
                    invoke-static { v0 }, $EXTENSION_CLASS_DESCRIPTOR->setYTShortsRepeatEnum(Ljava/lang/Enum;)V
                """,
            )
        }

        reelPlaybackRepeatFingerprint.method.apply {
            // The behavior enums are looked up from an ordinal value to an enum type.
            findInstructionIndicesReversedOrThrow {
                val reference = getReference<MethodReference>()
                reference?.definingClass == reelEnumClass &&
                    reference.parameterTypes.firstOrNull() == "I" &&
                    reference.returnType == reelEnumClass
            }.forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index + 1).registerA

                addInstructions(
                    index + 2,
                    """
                        invoke-static {v$register}, $EXTENSION_CLASS_DESCRIPTOR->changeShortsRepeatBehavior(Ljava/lang/Enum;)Ljava/lang/Enum;
                        move-result-object v$register
                    """,
                )
            }
        }
    }
}
