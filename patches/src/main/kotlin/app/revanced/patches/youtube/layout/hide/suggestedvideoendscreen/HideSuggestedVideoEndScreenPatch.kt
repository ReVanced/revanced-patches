package app.revanced.patches.youtube.layout.hide.suggestedvideoendscreen

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.fingerprint.matchOrThrow
import app.revanced.util.fingerprint.methodOrThrow
import app.revanced.util.getReference
import app.revanced.util.getWalkerMethod
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal var sizeAdjustableLiteAutoNavOverlay = -1L
    private set

internal val hideSuggestedVideoEndScreenResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "layout.hide.suggestedvideoendscreen.hideSuggestedVideoEndScreenResourcePatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_hide_suggested_video_end_screen"),
        )

        sizeAdjustableLiteAutoNavOverlay = resourceMappings[
            "layout",
            "size_adjustable_lite_autonav_overlay",
        ]
    }
}

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/HideSuggestedVideoEndScreenPatch;"

@Suppress("unused")
val hideSuggestedVideoEndScreenPatch = bytecodePatch(
    name = "Hide suggested video end screen",
    description = "Adds an option to hide the suggested video end screen at the end of videos.",
) {
    dependsOn(
        sharedExtensionPatch,
        hideSuggestedVideoEndScreenResourcePatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
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
        createEndScreenViewFingerprint.method.apply {
            val addOnClickEventListenerIndex = createEndScreenViewFingerprint.patternMatch!!.endIndex - 1
            val viewRegister = getInstruction<FiveRegisterInstruction>(addOnClickEventListenerIndex).registerC

            addInstruction(
                addOnClickEventListenerIndex + 1,
                "invoke-static {v$viewRegister}, " +
                    "$EXTENSION_CLASS_DESCRIPTOR->closeEndScreen(Landroid/widget/ImageView;)V",
            )
        }
        removeOnLayoutChangeListenerFingerprint.matchOrThrow().let {
            val walkerIndex =
                it.getWalkerMethod(it.patternMatch!!.endIndex)

            walkerIndex.apply {
                val autoNavStatusMethodName =
                    autoNavStatusFingerprint.methodOrThrow(autoNavConstructorFingerprint).name
                val invokeIndex =
                    indexOfFirstInstructionOrThrow {
                        val reference = getReference<MethodReference>()
                        reference?.returnType == "Z" &&
                                reference.parameterTypes.isEmpty() &&
                                reference.name == autoNavStatusMethodName
                    }
                val iGetObjectIndex =
                    indexOfFirstInstructionReversedOrThrow(invokeIndex, Opcode.IGET_OBJECT)

                val invokeReference = getInstruction<ReferenceInstruction>(invokeIndex).reference
                val iGetObjectReference =
                    getInstruction<ReferenceInstruction>(iGetObjectIndex).reference
                val opcodeName = getInstruction(invokeIndex).opcode.name

                addInstructionsWithLabels(
                    0,
                    """
                        invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->hideSuggestedVideoEndScreen()Z
                        move-result v0
                        if-eqz v0, :show_suggested_video_end_screen

                        iget-object v0, p0, $iGetObjectReference

                        # This reference checks whether autoplay is turned on.
                        $opcodeName {v0}, $invokeReference
                        move-result v0

                        # Hide suggested video end screen only when autoplay is turned off.
                        if-nez v0, :show_suggested_video_end_screen
                        return-void
                        """,
                    ExternalLabel(
                        "show_suggested_video_end_screen",
                        getInstruction(0)
    }
}
