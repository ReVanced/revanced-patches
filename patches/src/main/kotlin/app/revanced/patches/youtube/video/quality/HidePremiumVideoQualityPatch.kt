package app.revanced.patches.youtube.video.quality

import app.revanced.patcher.accessFlags
import app.revanced.patcher.afterAtMost
import app.revanced.patcher.allOf
import app.revanced.patcher.custom
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.fieldReference
import app.revanced.patcher.field
import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.returnType
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.video.information.EXTENSION_VIDEO_QUALITY_INTERFACE
import app.revanced.patches.youtube.video.information.videoInformationPatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/playback/quality/HidePremiumVideoQualityPatch;"

internal val hidePremiumVideoQualityPatch = bytecodePatch {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        videoInformationPatch,
        addResourcesPatch
    )

    apply {
        addResources("youtube", "video.quality.hidePremiumVideoQualityPatch")

        settingsMenuVideoQualityGroup.add(
            SwitchPreference("revanced_hide_premium_video_quality")
        )

        // Class name is obfuscated in 21.02+.
        val videoQualityArrayFieldType = defaultOverflowOverlayOnClickMethodMatch.let {
            it.method.getInstruction(it[-1]).fieldReference!!.type
        }

        // To avoid ClassCastException, declare the new array
        // as original video quality class instead of EXTENSION_VIDEO_QUALITY_INTERFACE.
        hidePremiumVideoQualityGetArrayMethod.addInstructions(
            0,
            """
                new-array p1, p1, $videoQualityArrayFieldType
                return-object p1
            """
        )

        fun ClassDef.getCurrentVideoFormatConstructorMethodMatch() = firstMethodComposite {
            accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
            returnType("V")

            var methodDefiningClass = ""
            custom {
                methodDefiningClass = definingClass
                true
            }

            instructions(
                allOf(
                    Opcode.IPUT_OBJECT(),
                    field { type == videoQualityArrayFieldType && definingClass == methodDefiningClass }
                )
            )
        }

        currentVideoFormatToStringMethod.immutableClassDef.getCurrentVideoFormatConstructorMethodMatch()
            .let {
                it.method.apply {
                    val index = it[-1]
                    val register = getInstruction<TwoRegisterInstruction>(index).registerA

                    addInstructions(
                        index,
                        """
                            invoke-static/range { v$register .. v$register }, $EXTENSION_CLASS_DESCRIPTOR->hidePremiumVideoQuality([$EXTENSION_VIDEO_QUALITY_INTERFACE)[Ljava/lang/Object;
                            move-result-object v$register
                            check-cast v$register, $videoQualityArrayFieldType
                        """
                    )
                }
            }
    }
}
