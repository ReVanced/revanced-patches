package app.revanced.patches.music.layout.branding

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.music.misc.gms.Constants.MUSIC_MAIN_ACTIVITY_NAME
import app.revanced.patches.music.misc.gms.Constants.MUSIC_PACKAGE_NAME
import app.revanced.patches.music.misc.gms.musicActivityOnCreateFingerprint
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.shared.layout.branding.baseCustomBrandingPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversed
import app.revanced.util.indexOfFirstLiteralInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private val disableSplashAnimationPatch = bytecodePatch {

    dependsOn(resourceMappingPatch)

    execute {
        // The existing YT animation usually only shows for a fraction of a second,
        // and the existing animation does not match the new splash screen
        // causing the original YT Music logo to momentarily flash on screen as the animation starts.
        //
        // Could replace the lottie animation file with our own custom animation (app_launch.json),
        // but the animation is not always the same size as the launch screen and it's still
        // barely shown. Instead turn off the animation entirely (app will also launch a little faster).
        cairoSplashAnimationConfigFingerprint.method.apply {
            val mainActivityLaunchAnimation = resourceMappings["layout", "main_activity_launch_animation"]
            val literalIndex = indexOfFirstLiteralInstructionOrThrow(
                mainActivityLaunchAnimation
            )
            val insertIndex = indexOfFirstInstructionReversed(literalIndex) {
                this.opcode == Opcode.INVOKE_VIRTUAL &&
                        getReference<MethodReference>()?.name == "setContentView"
            } + 1
            val jumpIndex = indexOfFirstInstructionOrThrow(insertIndex) {
                opcode == Opcode.INVOKE_VIRTUAL &&
                        getReference<MethodReference>()?.parameterTypes?.firstOrNull() == "Ljava/lang/Runnable;"
            } + 1

            addInstructionsWithLabels(
                insertIndex,
                "goto :skip_animation",
                ExternalLabel("skip_animation", getInstruction(jumpIndex))
            )
        }
    }
}

@Suppress("unused")
val customBrandingPatch = baseCustomBrandingPatch(
    addResourcePatchName = "music",
    originalLauncherIconName = "ic_launcher_release",
    originalAppName = "@string/app_launcher_name",
    originalAppPackageName = MUSIC_PACKAGE_NAME,
    copyExistingIntentsToAliases = false,
    numberOfPresetAppNames = 5,
    mainActivityOnCreateFingerprint = musicActivityOnCreateFingerprint,
    mainActivityName = MUSIC_MAIN_ACTIVITY_NAME,
    activityAliasNameWithIntents = MUSIC_MAIN_ACTIVITY_NAME,
    preferenceScreen = PreferenceScreen.GENERAL,

    block = {
        dependsOn(disableSplashAnimationPatch)

        compatibleWith(
            "com.google.android.apps.youtube.music"(
                "7.29.52",
                "8.10.52"
            )
        )
    }
)
