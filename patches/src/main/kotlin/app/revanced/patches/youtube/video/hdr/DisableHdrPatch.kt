package app.revanced.patches.youtube.video.hdr

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/DisableHdrPatch;"

@Suppress("unused")
val disableHdrPatch = bytecodePatch(
    name = "Disable HDR video",
    description = "Adds an option to disable video HDR.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.34.42",
            "19.43.41",
            "19.47.53",
            "20.07.39",
            "20.12.46",
            "20.13.41",
        )
    )

    execute {
        addResources("youtube", "video.hdr.disableHdrPatch")

        PreferenceScreen.VIDEO.addPreferences(
            SwitchPreference("revanced_disable_hdr_video")
        )

        hdrCapabilityFingerprint.let {
            it.originalMethod.apply {
                val stringIndex = it.stringMatches!!.first().index
                val navigateIndex = indexOfFirstInstructionOrThrow(stringIndex) {
                    val reference = getReference<MethodReference>()
                    reference?.parameterTypes == listOf("I", "Landroid/view/Display;") &&
                            reference.returnType == "Z"
                }

                // Modify the HDR lookup method (Method is in the same class as the fingerprint).
                navigate(this).to(navigateIndex).stop().addInstructionsWithLabels(
                    0,
                    """
                        invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->disableHDRVideo()Z
                        move-result v0
                        if-nez v0, :useHdr
                        return v0
                        :useHdr
                        nop 
                     """
                )
            }
        }
    }
}
