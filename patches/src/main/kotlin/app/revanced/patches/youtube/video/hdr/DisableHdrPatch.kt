package app.revanced.patches.youtube.video.hdr

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

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
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.47.53",
            "20.07.39",
            "20.12.46",
        )
    )

    execute {
        addResources("youtube", "video.hdr.disableHdrPatch")

        PreferenceScreen.VIDEO.addPreferences(
            SwitchPreference("revanced_disable_hdr_video")
        )

        hdrCapabilityFingerprint.let {
            it.originalMethod.apply {
                val navigateIndex = it.instructionMatches.last().index

                // Modify the HDR lookup method (Method is in the same class as the fingerprint class).
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
