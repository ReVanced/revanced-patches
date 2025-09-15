package app.revanced.patches.music.misc.spoof

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.gms.musicActivityOnCreateFingerprint
import app.revanced.patches.music.playservice.is_7_33_or_greater
import app.revanced.patches.music.playservice.is_8_11_or_greater
import app.revanced.patches.music.playservice.is_8_15_or_greater
import app.revanced.patches.music.playservice.versionCheckPatch
import app.revanced.patches.shared.misc.spoof.spoofVideoStreamsPatch

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/music/patches/spoof/SpoofVideoStreamsPatch;"

val spoofVideoStreamsPatch = spoofVideoStreamsPatch(
    fixMediaFetchHotConfigChanges = { true },
    fixMediaFetchHotConfigAlternativeChanges = { is_8_11_or_greater && !is_8_15_or_greater },
    fixParsePlaybackResponseFeatureFlag = { is_7_33_or_greater },
    block = {
        compatibleWith(
            "com.google.android.apps.youtube.music"(
                "7.29.52"
            )
        )

        dependsOn(sharedExtensionPatch, versionCheckPatch, userAgentClientSpoofPatch)
    },
    executeBlock = {
        musicActivityOnCreateFingerprint.method.addInstruction(
            1, // Must use 1 index so context is set by extension patch.
            "invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->setClientOrderToUse()V"
        )
    }
)