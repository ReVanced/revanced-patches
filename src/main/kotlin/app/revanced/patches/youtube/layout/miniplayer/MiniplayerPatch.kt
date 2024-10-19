@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.youtube.layout.miniplayer

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.BasePreference
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.layout.miniplayer.MiniplayerResourcePatch.modernMiniplayerClose
import app.revanced.patches.youtube.layout.miniplayer.MiniplayerResourcePatch.modernMiniplayerExpand
import app.revanced.patches.youtube.layout.miniplayer.MiniplayerResourcePatch.modernMiniplayerForwardButton
import app.revanced.patches.youtube.layout.miniplayer.MiniplayerResourcePatch.modernMiniplayerRewindButton
import app.revanced.patches.youtube.layout.miniplayer.MiniplayerResourcePatch.scrimOverlay
import app.revanced.patches.youtube.layout.miniplayer.MiniplayerResourcePatch.ytOutlinePictureInPictureWhite24
import app.revanced.patches.youtube.layout.miniplayer.MiniplayerResourcePatch.ytOutlineXWhite24
import app.revanced.patches.youtube.layout.miniplayer.fingerprints.MiniplayerDimensionsCalculatorParentFingerprint
import app.revanced.patches.youtube.layout.miniplayer.fingerprints.MiniplayerMinimumSizeFingerprint
import app.revanced.patches.youtube.layout.miniplayer.fingerprints.MiniplayerModernAddViewListenerFingerprint
import app.revanced.patches.youtube.layout.miniplayer.fingerprints.MiniplayerModernCloseButtonFingerprint
import app.revanced.patches.youtube.layout.miniplayer.fingerprints.MiniplayerModernConstructorFingerprint
import app.revanced.patches.youtube.layout.miniplayer.fingerprints.MiniplayerModernExpandButtonFingerprint
import app.revanced.patches.youtube.layout.miniplayer.fingerprints.MiniplayerModernExpandCloseDrawablesFingerprint
import app.revanced.patches.youtube.layout.miniplayer.fingerprints.MiniplayerModernForwardButtonFingerprint
import app.revanced.patches.youtube.layout.miniplayer.fingerprints.MiniplayerModernOverlayViewFingerprint
import app.revanced.patches.youtube.layout.miniplayer.fingerprints.MiniplayerModernRewindButtonFingerprint
import app.revanced.patches.youtube.layout.miniplayer.fingerprints.MiniplayerModernViewParentFingerprint
import app.revanced.patches.youtube.layout.miniplayer.fingerprints.MiniplayerOverrideFingerprint
import app.revanced.patches.youtube.layout.miniplayer.fingerprints.MiniplayerOverrideNoContextFingerprint
import app.revanced.patches.youtube.layout.miniplayer.fingerprints.MiniplayerResponseModelSizeCheckFingerprint
import app.revanced.patches.youtube.layout.miniplayer.fingerprints.YouTubePlayerOverlaysLayoutFingerprint
import app.revanced.patches.youtube.layout.miniplayer.fingerprints.YouTubePlayerOverlaysLayoutFingerprint.YOUTUBE_PLAYER_OVERLAYS_LAYOUT_CLASS_NAME
import app.revanced.patches.youtube.layout.tablet.fingerprints.GetFormFactorFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.playservice.VersionCheckPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.alsoResolve
import app.revanced.util.findOpcodeIndicesReversed
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstWideLiteralInstructionValueOrThrow
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

// YT uses "Miniplayer" without a space between 'mini' and 'player': https://support.google.com/youtube/answer/9162927.
@Patch(
    name = "Miniplayer",
    description = "Adds options to change the in app minimized player. " +
            "Patching target 19.16+ adds modern miniplayers.",
    dependencies = [
        IntegrationsPatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class,
        MiniplayerResourcePatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.38.44",
                "18.49.37",
                // 19.14.43 // Incomplete code for modern miniplayers.
                // 19.15.36 // Different code for handling subtitle texts and not worth supporting.
                "19.16.39", // First with modern miniplayers.
                // 19.17.41 // Works without issues, but no reason to recommend over 19.16.
                // 19.18.41 // Works without issues, but no reason to recommend over 19.16.
                // 19.19.39 // Last bug free version with smaller Modern 1 miniplayer, but no reason to recommend over 19.16.
                // 19.20.35 // Cannot swipe to expand.
                // 19.21.40 // Cannot swipe to expand.
                // 19.22.43 // Cannot swipe to expand.
                // 19.23.40 // First with Modern 1 drag and drop, Cannot swipe to expand.
                // 19.24.45 // First with larger Modern 1, Cannot swipe to expand.
                "19.25.37", // First with double tap, last with skip forward/back buttons, last with swipe to expand/close, and last before double tap to expand seems to be required.
                // 19.26.42 // Modern 1 Pause/play button are always hidden. Unusable.
                // 19.28.42 // First with custom miniplayer size, screen flickers when swiping to maximize Modern 1. Swipe to close miniplayer is broken.
                // 19.29.42 // All modern players are broken and ignore tapping the miniplayer video.
                // 19.30.39 // Modern 3 is less broken when double tap expand is enabled, but cannot swipe to expand when double tap is off.
                // 19.31.36 // All Modern 1 buttons are missing. Unusable.
                // 19.32.36 // 19.32+ and beyond all work without issues.
                // 19.33.35
                "19.34.42",
            ]
        )
    ]
)
@Suppress("unused")
object MiniplayerPatch : BytecodePatch(
    setOf(GetFormFactorFingerprint,
        MiniplayerDimensionsCalculatorParentFingerprint,
        MiniplayerResponseModelSizeCheckFingerprint,
        MiniplayerOverrideFingerprint,
        MiniplayerModernConstructorFingerprint,
        MiniplayerModernViewParentFingerprint,
        MiniplayerMinimumSizeFingerprint,
        YouTubePlayerOverlaysLayoutFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/MiniplayerPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        val preferences = mutableSetOf<BasePreference>()

        if (!VersionCheckPatch.is_19_16_or_greater) {
            preferences += ListPreference(
                "revanced_miniplayer_type",
                summaryKey = null,
                entriesKey = "revanced_miniplayer_type_legacy_entries",
                entryValuesKey = "revanced_miniplayer_type_legacy_entry_values"
            )
        } else {
            preferences += ListPreference(
                "revanced_miniplayer_type",
                summaryKey = null,
            )

            if (VersionCheckPatch.is_19_25_or_greater) {
                if (!VersionCheckPatch.is_19_29_or_greater) {
                    preferences += SwitchPreference("revanced_miniplayer_double_tap_action")
                }
                preferences += SwitchPreference("revanced_miniplayer_drag_and_drop")
            }

            if (VersionCheckPatch.is_19_36_or_greater) {
                preferences += SwitchPreference("revanced_miniplayer_rounded_corners")
            }

            preferences += SwitchPreference("revanced_miniplayer_hide_subtext")

            preferences +=
                if (VersionCheckPatch.is_19_26_or_greater) {
                    SwitchPreference("revanced_miniplayer_hide_expand_close")
                } else {
                    SwitchPreference(
                        key = "revanced_miniplayer_hide_expand_close",
                        titleKey = "revanced_miniplayer_hide_expand_close_legacy_title",
                        summaryOnKey = "revanced_miniplayer_hide_expand_close_legacy_summary_on",
                        summaryOffKey = "revanced_miniplayer_hide_expand_close_legacy_summary_off",
                    )
                }

            if (!VersionCheckPatch.is_19_26_or_greater) {
                preferences += SwitchPreference("revanced_miniplayer_hide_rewind_forward")
            }

            if (VersionCheckPatch.is_19_26_or_greater) {
                preferences += TextPreference("revanced_miniplayer_width_dip", inputType = InputType.NUMBER)
            }

            preferences += TextPreference("revanced_miniplayer_opacity", inputType = InputType.NUMBER)
        }

        SettingsPatch.PreferenceScreen.PLAYER.addPreferences(
            PreferenceScreen(
                key = "revanced_miniplayer_screen",
                sorting = Sorting.UNSORTED,
                preferences = preferences
            )
        )

        // region Enable tablet miniplayer.

        MiniplayerOverrideNoContextFingerprint.alsoResolve(
            context,
            MiniplayerDimensionsCalculatorParentFingerprint
        ).mutableMethod.apply {
            findReturnIndicesReversed().forEach { index -> insertLegacyTabletMiniplayerOverride(index) }
        }

        // endregion

        // region Legacy tablet Miniplayer hooks.

        MiniplayerOverrideFingerprint.resultOrThrow().let {
            val appNameStringIndex = it.scanResult.stringsScanResult!!.matches.first().index + 2

            it.mutableMethod.apply {
                val walkerMethod = context.toMethodWalker(this)
                    .nextMethod(appNameStringIndex, true)
                    .getMethod() as MutableMethod

                walkerMethod.apply {
                    findReturnIndicesReversed().forEach { index -> insertLegacyTabletMiniplayerOverride(index) }
                }
            }
        }

        MiniplayerResponseModelSizeCheckFingerprint.resultOrThrow().let {
            it.mutableMethod.insertLegacyTabletMiniplayerOverride(it.scanResult.patternScanResult!!.endIndex)
        }

        if (!VersionCheckPatch.is_19_16_or_greater) {
            // Return here, as patch below is only for the current versions of the app.
            return
        }

        // endregion


        // region Enable modern miniplayer.

        MiniplayerModernConstructorFingerprint.resultOrThrow().mutableClass.methods.forEach {
            it.apply {
                if (AccessFlags.CONSTRUCTOR.isSet(accessFlags)) {
                    val iPutIndex = indexOfFirstInstructionOrThrow {
                        this.opcode == Opcode.IPUT && this.getReference<FieldReference>()?.type == "I"
                    }

                    insertModernMiniplayerTypeOverride(iPutIndex)
                } else {
                    findReturnIndicesReversed().forEach { index -> insertModernMiniplayerOverride(index) }
                }
            }
        }

        if (VersionCheckPatch.is_19_23_or_greater) {
            MiniplayerModernConstructorFingerprint.insertLiteralValueBooleanOverride(
                MiniplayerModernConstructorFingerprint.DRAG_DROP_ENABLED_FEATURE_KEY_LITERAL,
                "enableMiniplayerDragAndDrop"
            )
        }

        if (VersionCheckPatch.is_19_25_or_greater) {
            MiniplayerModernConstructorFingerprint.insertLiteralValueBooleanOverride(
                MiniplayerModernConstructorFingerprint.MODERN_MINIPLAYER_ENABLED_OLD_TARGETS_FEATURE_KEY,
                "getModernMiniplayerOverride"
            )

            MiniplayerModernConstructorFingerprint.insertLiteralValueBooleanOverride(
                MiniplayerModernConstructorFingerprint.MODERN_FEATURE_FLAGS_ENABLED_KEY_LITERAL,
                "getModernFeatureFlagsActiveOverride"
            )

            MiniplayerModernConstructorFingerprint.insertLiteralValueBooleanOverride(
                MiniplayerModernConstructorFingerprint.DOUBLE_TAP_ENABLED_FEATURE_KEY_LITERAL,
                "enableMiniplayerDoubleTapAction"
            )
        }

        if (VersionCheckPatch.is_19_26_or_greater) {
            MiniplayerModernConstructorFingerprint.resultOrThrow().mutableMethod.apply {
                val literalIndex = indexOfFirstWideLiteralInstructionValueOrThrow(
                    MiniplayerModernConstructorFingerprint.INITIAL_SIZE_FEATURE_KEY_LITERAL
                )
                val targetIndex = indexOfFirstInstructionOrThrow(literalIndex, Opcode.LONG_TO_INT)

                val register = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstructions(
                    targetIndex + 1,
                    """
                        invoke-static { v$register }, $INTEGRATIONS_CLASS_DESCRIPTOR->setMiniplayerDefaultSize(I)I
                        move-result v$register
                    """
                )
            }

            // Override a mininimum miniplayer size constant.
            MiniplayerMinimumSizeFingerprint.resultOrThrow().mutableMethod.apply {
                val index = indexOfFirstInstructionOrThrow {
                    opcode == Opcode.CONST_16 && (this as NarrowLiteralInstruction).narrowLiteral == 192
                }
                val register = getInstruction<OneRegisterInstruction>(index).registerA

                // Smaller sizes can be used, but the miniplayer will always start in size 170 if set any smaller.
                // The 170 initial limit probably could be patched to allow even smaller initial sizes,
                // but 170 is already half the horizontal space and smaller does not seem useful.
                replaceInstruction(index, "const/16 v$register, 170")
            }
        }

        if (VersionCheckPatch.is_19_32_or_greater) {
            // Feature is not exposed in the settings, and currently only for debugging.

            MiniplayerModernConstructorFingerprint.insertLiteralValueFloatOverride(
                MiniplayerModernConstructorFingerprint.ANIMATION_INTERPOLATION_FEATURE_KEY,
                "setMovementBoundFactor"
            )
        }

        if (VersionCheckPatch.is_19_36_or_greater) {
            MiniplayerModernConstructorFingerprint.insertLiteralValueBooleanOverride(
                MiniplayerModernConstructorFingerprint.DROP_SHADOW_FEATURE_KEY,
                "setDropShadow"
            )

            MiniplayerModernConstructorFingerprint.insertLiteralValueBooleanOverride(
                MiniplayerModernConstructorFingerprint.ROUNDED_CORNERS_FEATURE_KEY,
                "setRoundedCorners"
            )
        }

        // endregion

        // region Fix 19.16 using mixed up drawables for tablet modern.
        // YT fixed this mistake in 19.17.
        // Fix this, by swapping the drawable resource values with each other.
        if (ytOutlinePictureInPictureWhite24 >= 0) {
            MiniplayerModernExpandCloseDrawablesFingerprint.alsoResolve(
                context,
                MiniplayerModernViewParentFingerprint
            ).mutableMethod.apply {
                listOf(
                    ytOutlinePictureInPictureWhite24 to ytOutlineXWhite24,
                    ytOutlineXWhite24 to ytOutlinePictureInPictureWhite24,
                ).forEach { (originalResource, replacementResource) ->
                    val imageResourceIndex = indexOfFirstWideLiteralInstructionValueOrThrow(originalResource)
                    val register = getInstruction<OneRegisterInstruction>(imageResourceIndex).registerA

                    replaceInstruction(imageResourceIndex, "const v$register, $replacementResource")
                }
            }
        }

        // endregion

        // region Add hooks to hide modern miniplayer buttons.

        listOf(
            Triple(
                MiniplayerModernExpandButtonFingerprint,
                modernMiniplayerExpand,
                "hideMiniplayerExpandClose"
            ),
            Triple(
                MiniplayerModernCloseButtonFingerprint,
                modernMiniplayerClose,
                "hideMiniplayerExpandClose"
            ),
            Triple(
                MiniplayerModernRewindButtonFingerprint,
                modernMiniplayerRewindButton,
                "hideMiniplayerRewindForward"
            ),
            Triple(
                MiniplayerModernForwardButtonFingerprint,
                modernMiniplayerForwardButton,
                "hideMiniplayerRewindForward"
            ),
            Triple(
                MiniplayerModernOverlayViewFingerprint,
                scrimOverlay,
                "adjustMiniplayerOpacity"
            )
        ).forEach { (fingerprint, literalValue, methodName) ->
            fingerprint.alsoResolve(
                context,
                MiniplayerModernViewParentFingerprint
            ).mutableMethod.hookInflatedView(
                literalValue,
                "Landroid/widget/ImageView;",
                "$INTEGRATIONS_CLASS_DESCRIPTOR->$methodName(Landroid/widget/ImageView;)V"
            )
        }

        MiniplayerModernAddViewListenerFingerprint.alsoResolve(
            context,
            MiniplayerModernViewParentFingerprint
        ).mutableMethod.addInstruction(
            0,
            "invoke-static { p1 }, $INTEGRATIONS_CLASS_DESCRIPTOR->" +
                    "hideMiniplayerSubTexts(Landroid/view/View;)V"
        )


        // Modern 2 has a broken overlay subtitle view that is always present.
        // Modern 2 uses the same overlay controls as the regular video player,
        // and the overlay views are added at runtime.
        // Add a hook to the overlay class, and pass the added views to integrations.
        //
        // NOTE: Modern 2 uses the same video UI as the regular player except resized to smaller.
        // This patch code could be used to hide other player overlays that do not use Litho.
        YouTubePlayerOverlaysLayoutFingerprint.resultOrThrow().mutableClass.methods.add(
            ImmutableMethod(
                YOUTUBE_PLAYER_OVERLAYS_LAYOUT_CLASS_NAME,
                "addView",
                listOf(
                    ImmutableMethodParameter("Landroid/view/View;", null, null),
                    ImmutableMethodParameter("I", null, null),
                    ImmutableMethodParameter("Landroid/view/ViewGroup\$LayoutParams;", null, null),
                ),
                "V",
                AccessFlags.PUBLIC.value,
                null,
                null,
                MutableMethodImplementation(4),
            ).toMutable().apply {
                addInstructions(
                    """
                        invoke-super { p0, p1, p2, p3 }, Landroid/view/ViewGroup;->addView(Landroid/view/View;ILandroid/view/ViewGroup${'$'}LayoutParams;)V
                        invoke-static { p1 }, $INTEGRATIONS_CLASS_DESCRIPTOR->playerOverlayGroupCreated(Landroid/view/View;)V
                        return-void
                    """,
                )
            }
        )

        // endregion
    }

    private fun Method.findReturnIndicesReversed() = findOpcodeIndicesReversed(Opcode.RETURN)

    /**
     * Adds an override to force legacy tablet miniplayer to be used or not used.
     */
    private fun MutableMethod.insertLegacyTabletMiniplayerOverride(index: Int) {
        insertBooleanOverride(index, "getLegacyTabletMiniplayerOverride")
    }

    /**
     * Adds an override to force modern miniplayer to be used or not used.
     */
    private fun MutableMethod.insertModernMiniplayerOverride(index: Int) {
        insertBooleanOverride(index, "getModernMiniplayerOverride")
    }

    private fun MethodFingerprint.insertLiteralValueBooleanOverride(
        literal: Long,
        integrationsMethod: String
    ) {
        resultOrThrow().mutableMethod.apply {
            val literalIndex = indexOfFirstWideLiteralInstructionValueOrThrow(literal)
            val targetIndex = indexOfFirstInstructionOrThrow(literalIndex, Opcode.MOVE_RESULT)

            insertBooleanOverride(targetIndex + 1, integrationsMethod)
        }
    }

    private fun MethodFingerprint.insertLiteralValueFloatOverride(
        literal: Long,
        integrationsMethod: String
    ) {
        resultOrThrow().mutableMethod.apply {
            val literalIndex = indexOfFirstWideLiteralInstructionValueOrThrow(literal)
            val targetIndex = indexOfFirstInstructionOrThrow(literalIndex, Opcode.DOUBLE_TO_FLOAT)
            val register = getInstruction<OneRegisterInstruction>(targetIndex).registerA

            addInstructions(
                targetIndex + 1,
                """
                        invoke-static {v$register}, $INTEGRATIONS_CLASS_DESCRIPTOR->$integrationsMethod(F)F
                        move-result v$register
                    """
            )
        }
    }

    private fun MutableMethod.insertBooleanOverride(index: Int, methodName: String) {
        val register = getInstruction<OneRegisterInstruction>(index).registerA
        addInstructions(
            index,
            """
                invoke-static {v$register}, $INTEGRATIONS_CLASS_DESCRIPTOR->$methodName(Z)Z
                move-result v$register
            """
        )
    }

    /**
     * Adds an override to specify which modern miniplayer is used.
     */
    private fun MutableMethod.insertModernMiniplayerTypeOverride(iPutIndex: Int) {
        val targetInstruction = getInstruction<TwoRegisterInstruction>(iPutIndex)

        addInstructionsAtControlFlowLabel(
            iPutIndex, """
                invoke-static { v${targetInstruction.registerA} }, $INTEGRATIONS_CLASS_DESCRIPTOR->getModernMiniplayerOverrideType(I)I
                move-result v${targetInstruction.registerA}
            """
        )
    }

    private fun MutableMethod.hookInflatedView(
        literalValue: Long,
        hookedClassType: String,
        integrationsMethodName: String,
    ) {
        val imageViewIndex = indexOfFirstInstructionOrThrow(
            indexOfFirstWideLiteralInstructionValueOrThrow(literalValue)
        ) {
            opcode == Opcode.CHECK_CAST && getReference<TypeReference>()?.type == hookedClassType
        }

        val register = getInstruction<OneRegisterInstruction>(imageViewIndex).registerA
        addInstruction(
            imageViewIndex + 1,
            "invoke-static { v$register }, $integrationsMethodName"
        )
    }
}
