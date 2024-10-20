package app.revanced.patches.youtube.layout.shortsautoplay

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.all.misc.resources.AddResourcesPatch.invoke
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.shortsautoplay.fingerprints.ReelEnumConstructorFingerprint
import app.revanced.patches.youtube.layout.shortsautoplay.fingerprints.ReelPlaybackRepeatFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.playservice.VersionCheckPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.shared.fingerprints.MainActivityOnCreateFingerprint
import app.revanced.util.findOpcodeIndicesReversed
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Shorts autoplay",
    description = "Adds options to automatically play the next Short.",
    dependencies = [
        IntegrationsPatch::class,
        SettingsPatch::class,
        ResourceMappingPatch::class,
        VersionCheckPatch::class,
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.49.37",
                "19.16.39",
                "19.25.37",
                "19.34.42",
            ],
        ),
    ],
)
@Suppress("unused")
object ShortsAutoplayPatch : BytecodePatch(
    setOf(
        MainActivityOnCreateFingerprint,
        ReelEnumConstructorFingerprint,
        ReelPlaybackRepeatFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/ShortsAutoplayPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.SHORTS.addPreferences(
            SwitchPreference("revanced_shorts_autoplay")
        )

        if (VersionCheckPatch.is_19_34_or_greater) {
            SettingsPatch.PreferenceScreen.SHORTS.addPreferences(
                SwitchPreference("revanced_shorts_autoplay_background")
            )
        }

        // Main activity is used to check if app is in pip mode.
        MainActivityOnCreateFingerprint.resultOrThrow().mutableMethod.addInstructions(
            0,
            "invoke-static/range { p0 .. p0 }, $INTEGRATIONS_CLASS_DESCRIPTOR->" +
                    "setMainActivity(Landroid/app/Activity;)V",
        )

        val reelEnumClass: String

        ReelEnumConstructorFingerprint.resultOrThrow().let {
            reelEnumClass = it.classDef.type

            it.mutableMethod.apply {
                val insertIndex = indexOfFirstInstructionOrThrow(Opcode.RETURN_VOID)

                addInstructions(
                    insertIndex,
                    """
                        # Pass the first enum value to integrations.
                        # Any enum value of this type will work.
                        sget-object v0, $reelEnumClass->a:$reelEnumClass
                        invoke-static { v0 }, $INTEGRATIONS_CLASS_DESCRIPTOR->setYTShortsRepeatEnum(Ljava/lang/Enum;)V
                    """
                )
            }
        }

        ReelPlaybackRepeatFingerprint.resultOrThrow().mutableMethod.apply {
            // The behavior enums are looked up from an ordinal value to an enum type.
            findOpcodeIndicesReversed {
                val reference = getReference<MethodReference>()
                reference?.definingClass == reelEnumClass
                        && reference.parameterTypes.firstOrNull() == "I"
                        && reference.returnType == reelEnumClass
            }.forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index + 1).registerA

                addInstructions(
                    index + 2,
                    """
                        invoke-static {v$register}, $INTEGRATIONS_CLASS_DESCRIPTOR->changeShortsRepeatBehavior(Ljava/lang/Enum;)Ljava/lang/Enum;
                        move-result-object v$register
                    """
                )
            }
        }
    }
}
