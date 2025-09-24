package app.revanced.patches.youtube.video.codecs

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.all.misc.transformation.transformInstructionsPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/DisableVideoCodecsPatch;"

@Suppress("unused")
val disableVideoCodecsPatch = bytecodePatch(
    name = "Disable video codecs",
    description = "Adds options to disable HDR and VP9 codecs.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        /**
         * Override all calls of `getSupportedHdrTypes`.
         */
        transformInstructionsPatch(
            filterMap = filterMap@{ classDef, _, instruction, instructionIndex ->
                if (classDef.type.startsWith("Lapp/revanced/")) {
                    return@filterMap null
                }

                val reference = instruction.getReference<MethodReference>()
                if (reference?.definingClass =="Landroid/view/Display\$HdrCapabilities;"
                    && reference.name == "getSupportedHdrTypes") {
                    return@filterMap instruction to instructionIndex
                }
                return@filterMap null
            },
            transform = { method, entry ->
                val (instruction, index) = entry
                val register = (instruction as FiveRegisterInstruction).registerC

                method.replaceInstruction(
                    index,
                    "invoke-static/range { v$register .. v$register }, $EXTENSION_CLASS_DESCRIPTOR->" +
                            "disableHdrVideo(Landroid/view/Display\$HdrCapabilities;)[I",
                )
            }
        )
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.34.42",
            "20.07.39",
            "20.13.41",
            "20.14.43",
        )
    )

    execute {
        addResources("youtube", "video.codecs.disableVideoCodecsPatch")

        PreferenceScreen.VIDEO.addPreferences(
            SwitchPreference("revanced_disable_hdr_video"),
            SwitchPreference("revanced_force_avc_codec")
        )

        vp9CapabilityFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->allowVP9()Z
                move-result v0
                if-nez v0, :default
                return v0
                :default
                nop
            """
        )
    }
}
