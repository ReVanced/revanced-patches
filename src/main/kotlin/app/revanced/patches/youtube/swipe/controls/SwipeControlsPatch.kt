package app.revanced.patches.youtube.swipe.controls

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.swipe.controls.fingerprints.FullScreenEngagementOverlayFingerprint
import app.revanced.patches.youtube.swipe.controls.fingerprints.HDRBrightnessFingerprint
import app.revanced.patches.youtube.swipe.controls.fingerprints.SwipeControlsHostActivityFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.SWIPE_PATH
import app.revanced.patches.youtube.utils.lockmodestate.LockModeStateHookPatch
import app.revanced.patches.youtube.utils.mainactivity.MainActivityResolvePatch
import app.revanced.patches.youtube.utils.mainactivity.MainActivityResolvePatch.mainActivityMutableClass
import app.revanced.patches.youtube.utils.playertype.PlayerTypeHookPatch
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.FullScreenEngagementOverlay
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch.contexts
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import app.revanced.util.exception
import app.revanced.util.getWideLiteralInstructionIndex
import app.revanced.util.transformMethods
import app.revanced.util.traverseClassHierarchy
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

@Patch(
    name = "Swipe controls",
    description = "Adds options to enable and configure volume and brightness swipe controls.",
    dependencies = [
        LockModeStateHookPatch::class,
        MainActivityResolvePatch::class,
        PlayerTypeHookPatch::class,
        SettingsPatch::class,
        SharedResourceIdPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object SwipeControlsPatch : BytecodePatch(
    setOf(
        FullScreenEngagementOverlayFingerprint,
        HDRBrightnessFingerprint,
        SwipeControlsHostActivityFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "$SWIPE_PATH/SwipeControlsPatch;"

    override fun execute(context: BytecodeContext) {
        val wrapperClass = SwipeControlsHostActivityFingerprint.result?.mutableClass
            ?: throw SwipeControlsHostActivityFingerprint.exception
        val targetClass = mainActivityMutableClass

        // inject the wrapper class from integrations into the class hierarchy of MainActivity (WatchWhileActivity)
        wrapperClass.setSuperClass(targetClass.superclass)
        targetClass.setSuperClass(wrapperClass.type)

        // ensure all classes and methods in the hierarchy are non-final, so we can override them in integrations
        context.traverseClassHierarchy(targetClass) {
            accessFlags = accessFlags and AccessFlags.FINAL.value.inv()
            transformMethods {
                ImmutableMethod(
                    definingClass,
                    name,
                    parameters,
                    returnType,
                    accessFlags and AccessFlags.FINAL.value.inv(),
                    annotations,
                    hiddenApiRestrictions,
                    implementation
                ).toMutable()
            }
        }

        FullScreenEngagementOverlayFingerprint.result?.let {
            it.mutableMethod.apply {
                val viewIndex = getWideLiteralInstructionIndex(FullScreenEngagementOverlay) + 3
                val viewRegister = getInstruction<OneRegisterInstruction>(viewIndex).registerA

                addInstruction(
                    viewIndex + 1,
                    "sput-object v$viewRegister, $INTEGRATIONS_CLASS_DESCRIPTOR->engagementOverlay:Landroid/view/View;"
                )
            }
        } ?: throw FullScreenEngagementOverlayFingerprint.exception

        HDRBrightnessFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstructionsWithLabels(
                    0, """
                        invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->disableHDRAutoBrightness()Z
                        move-result v0
                        if-eqz v0, :default
                        return-void
                        """, ExternalLabel("default", getInstruction(0))
                )
            }
        } ?: throw HDRBrightnessFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: SWIPE_SETTINGS",
                "SETTINGS: SWIPE_CONTROLS"
            )
        )

        SettingsPatch.updatePatchStatus("Swipe controls")

        contexts.copyResources(
            "youtube/swipecontrols",
            ResourceGroup(
                "drawable",
                "ic_sc_brightness_auto.xml",
                "ic_sc_brightness_manual.xml",
                "ic_sc_volume_mute.xml",
                "ic_sc_volume_normal.xml"
            )
        )
    }
}