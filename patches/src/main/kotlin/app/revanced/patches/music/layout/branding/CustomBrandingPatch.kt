package app.revanced.patches.music.layout.branding

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.gms.Constants.MUSIC_MAIN_ACTIVITY_NAME
import app.revanced.patches.music.misc.gms.Constants.MUSIC_PACKAGE_NAME
import app.revanced.patches.music.misc.gms.musicActivityOnCreateFingerprint
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.shared.layout.branding.EXTENSION_CLASS_DESCRIPTOR
import app.revanced.patches.shared.layout.branding.baseCustomBrandingPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstLiteralInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

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
            val literalIndex = indexOfFirstLiteralInstructionOrThrow(
                resourceMappings["layout", "main_activity_launch_animation"]
            )
            val checkCastIndex = indexOfFirstInstructionOrThrow(literalIndex) {
                opcode == Opcode.CHECK_CAST &&
                        getReference<TypeReference>()?.type == "Lcom/airbnb/lottie/LottieAnimationView;"
            }
            val register = getInstruction<OneRegisterInstruction>(checkCastIndex).registerA

            // If using a custom icon then set the lottie animation view to null to bypasses the startup animation.
            addInstructions(
                checkCastIndex,
                """
                    invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getLottieViewOrNull(Landroid/view/View;)Landroid/view/View;
                    move-result-object v$register
                """
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
    isYouTubeMusic = true,
    numberOfPresetAppNames = 5,
    mainActivityOnCreateFingerprint = musicActivityOnCreateFingerprint,
    mainActivityName = MUSIC_MAIN_ACTIVITY_NAME,
    activityAliasNameWithIntents = MUSIC_MAIN_ACTIVITY_NAME,
    preferenceScreen = PreferenceScreen.GENERAL,

    block = {
        dependsOn(sharedExtensionPatch, disableSplashAnimationPatch)

        compatibleWith(
            "com.google.android.apps.youtube.music"(
                "7.29.52",
                "8.10.52"
            )
        )
    }
)
