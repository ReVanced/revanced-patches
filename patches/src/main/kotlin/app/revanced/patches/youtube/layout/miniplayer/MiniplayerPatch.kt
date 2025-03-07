@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.youtube.layout.miniplayer

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.*
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.*
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.*
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

var floatyBarButtonTopMargin = -1L
    private set

// Only available in 19.15 and upwards.
var ytOutlineXWhite24 = -1L
    private set
var ytOutlinePictureInPictureWhite24 = -1L
    private set
var scrimOverlay = -1L
    private set
var modernMiniplayerClose = -1L
    private set
var modernMiniplayerExpand = -1L
    private set
var modernMiniplayerRewindButton = -1L
    private set
var modernMiniplayerForwardButton = -1L
    private set
var playerOverlays = -1L
    private set
var miniplayerMaxSize = -1L
    private set

private val miniplayerResourcePatch = resourcePatch {
    dependsOn(
        resourceMappingPatch,
        versionCheckPatch,
    )

    execute {
        floatyBarButtonTopMargin = resourceMappings[
            "dimen",
            "floaty_bar_button_top_margin",
        ]

        scrimOverlay = resourceMappings[
            "id",
            "scrim_overlay",
        ]

        playerOverlays = resourceMappings[
            "layout",
            "player_overlays",
        ]


        modernMiniplayerClose = resourceMappings[
            "id",
            "modern_miniplayer_close",
        ]

        modernMiniplayerExpand = resourceMappings[
            "id",
            "modern_miniplayer_expand",
        ]

        modernMiniplayerRewindButton = resourceMappings[
            "id",
            "modern_miniplayer_rewind_button",
        ]

        modernMiniplayerForwardButton = resourceMappings[
            "id",
            "modern_miniplayer_forward_button",
        ]

        // Resource id is not used during patching, but is used by extension.
        // Verify the resource is present while patching.
        resourceMappings[
            "id",
            "modern_miniplayer_subtitle_text",
        ]

        // Only required for exactly 19.16
        if (!is_19_17_or_greater) {
            ytOutlinePictureInPictureWhite24 = resourceMappings[
                "drawable",
                "yt_outline_picture_in_picture_white_24",
            ]

            ytOutlineXWhite24 = resourceMappings[
                "drawable",
                "yt_outline_x_white_24",
            ]
        }

        if (is_19_26_or_greater) {
            miniplayerMaxSize = resourceMappings[
                "dimen",
                "miniplayer_max_size",
            ]
        }
    }
}

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/MiniplayerPatch;"

@Suppress("unused")
val miniplayerPatch = bytecodePatch(
    name = "Miniplayer",
    description = "Adds options to change the in-app minimized player."
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        miniplayerResourcePatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
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
            "19.43.41",
            "19.45.38",
            "19.46.42",
            "19.47.53",
        ),
    )

    execute {
        addResources("youtube", "layout.miniplayer.miniplayerPatch")

        val preferences = mutableSetOf<BasePreference>()


        preferences +=
            if (is_19_43_or_greater) {
                ListPreference(
                    "revanced_miniplayer_type",
                    summaryKey = null,
                )
            } else {
                ListPreference(
                    "revanced_miniplayer_type",
                    summaryKey = null,
                    entriesKey = "revanced_miniplayer_type_legacy_entries",
                    entryValuesKey = "revanced_miniplayer_type_legacy_entry_values",
                )
            }

        if (is_19_25_or_greater) {
            if (!is_19_29_or_greater) {
                preferences += SwitchPreference("revanced_miniplayer_double_tap_action")
            }
            preferences += SwitchPreference("revanced_miniplayer_drag_and_drop")
        }

        if (is_19_43_or_greater) {
            preferences += SwitchPreference("revanced_miniplayer_horizontal_drag")
        }

        if (is_19_36_or_greater) {
            preferences += SwitchPreference("revanced_miniplayer_rounded_corners")
        }

        preferences += SwitchPreference("revanced_miniplayer_hide_subtext")

        preferences += if (is_19_26_or_greater) {
            SwitchPreference("revanced_miniplayer_hide_expand_close")
        } else {
            SwitchPreference(
                key = "revanced_miniplayer_hide_expand_close",
                titleKey = "revanced_miniplayer_hide_expand_close_legacy_title",
                summaryOnKey = "revanced_miniplayer_hide_expand_close_legacy_summary_on",
                summaryOffKey = "revanced_miniplayer_hide_expand_close_legacy_summary_off",
            )
        }

        if (!is_19_26_or_greater) {
            preferences += SwitchPreference("revanced_miniplayer_hide_rewind_forward")
        }

        if (is_19_26_or_greater) {
            preferences += TextPreference("revanced_miniplayer_width_dip", inputType = InputType.NUMBER)
        }

        preferences += TextPreference("revanced_miniplayer_opacity", inputType = InputType.NUMBER)

        PreferenceScreen.PLAYER.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_miniplayer_screen",
                sorting = PreferenceScreenPreference.Sorting.UNSORTED,
                preferences = preferences,
            ),
        )

        fun MutableMethod.insertMiniplayerBooleanOverride(index: Int, methodName: String) {
            val register = getInstruction<OneRegisterInstruction>(index).registerA
            addInstructions(
                index,
                """
                    invoke-static {v$register}, $EXTENSION_CLASS_DESCRIPTOR->$methodName(Z)Z
                    move-result v$register
                """
            )
        }

        fun Method.findReturnIndicesReversed() = findInstructionIndicesReversedOrThrow(Opcode.RETURN)

        /**
         * Adds an override to force legacy tablet miniplayer to be used or not used.
         */
        fun MutableMethod.insertLegacyTabletMiniplayerOverride(index: Int) {
            insertMiniplayerBooleanOverride(index, "getLegacyTabletMiniplayerOverride")
        }

        /**
         * Adds an override to force modern miniplayer to be used or not used.
         */
        fun MutableMethod.insertModernMiniplayerOverride(index: Int) {
            insertMiniplayerBooleanOverride(index, "getModernMiniplayerOverride")
        }

        fun Fingerprint.insertMiniplayerFeatureFlagBooleanOverride(
            literal: Long,
            extensionMethod: String,
        ) = method.insertFeatureFlagBooleanOverride(
            literal,
            "$EXTENSION_CLASS_DESCRIPTOR->$extensionMethod(Z)Z"
        )

        fun Fingerprint.insertMiniplayerFeatureFlagFloatOverride(
            literal: Long,
            extensionMethod: String,
        ) {
            method.apply {
                val literalIndex = indexOfFirstLiteralInstructionOrThrow(literal)
                val targetIndex = indexOfFirstInstructionOrThrow(literalIndex, Opcode.DOUBLE_TO_FLOAT)
                val register = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstructions(
                    targetIndex + 1,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->$extensionMethod(F)F
                        move-result v$register
                    """,
                )
            }
        }

        /**
         * Adds an override to specify which modern miniplayer is used.
         */
        fun MutableMethod.insertModernMiniplayerTypeOverride(iPutIndex: Int) {
            val register = getInstruction<TwoRegisterInstruction>(iPutIndex).registerA

            addInstructionsAtControlFlowLabel(
                iPutIndex,
                """
                    invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getModernMiniplayerOverrideType(I)I
                    move-result v$register
                """,
            )
        }

        fun MutableMethod.hookInflatedView(
            literalValue: Long,
            hookedClassType: String,
            extensionMethodName: String,
        ) {
            val imageViewIndex = indexOfFirstInstructionOrThrow(
                indexOfFirstLiteralInstructionOrThrow(literalValue),
            ) {
                opcode == Opcode.CHECK_CAST && getReference<TypeReference>()?.type == hookedClassType
            }

            val register = getInstruction<OneRegisterInstruction>(imageViewIndex).registerA
            addInstruction(
                imageViewIndex + 1,
                "invoke-static { v$register }, $extensionMethodName",
            )
        }

        // region Enable tablet miniplayer.

        miniplayerOverrideNoContextFingerprint.match(
            miniplayerDimensionsCalculatorParentFingerprint.originalClassDef,
        ).method.apply {
            findReturnIndicesReversed().forEach { index -> insertLegacyTabletMiniplayerOverride(index) }
        }

        // endregion

        // region Legacy tablet miniplayer hooks.
        val appNameStringIndex = miniplayerOverrideFingerprint.stringMatches!!.first().index + 2
        navigate(miniplayerOverrideFingerprint.originalMethod).to(appNameStringIndex).stop().apply {
            findReturnIndicesReversed().forEach { index -> insertLegacyTabletMiniplayerOverride(index) }
        }

        miniplayerResponseModelSizeCheckFingerprint.let {
            it.method.insertLegacyTabletMiniplayerOverride(it.patternMatch!!.endIndex)
        }

        // endregion

        // region Enable modern miniplayer.

        miniplayerModernConstructorFingerprint.classDef.methods.forEach {
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

        if (is_19_23_or_greater) {
            miniplayerModernConstructorFingerprint.insertMiniplayerFeatureFlagBooleanOverride(
                MINIPLAYER_DRAG_DROP_FEATURE_KEY,
                "enableMiniplayerDragAndDrop",
            )
        }

        if (is_19_25_or_greater) {
            miniplayerModernConstructorFingerprint.insertMiniplayerFeatureFlagBooleanOverride(
                MINIPLAYER_MODERN_FEATURE_LEGACY_KEY,
                "getModernMiniplayerOverride",
            )

            miniplayerModernConstructorFingerprint.insertMiniplayerFeatureFlagBooleanOverride(
                MINIPLAYER_MODERN_FEATURE_KEY,
                "getModernFeatureFlagsActiveOverride",
            )

            miniplayerModernConstructorFingerprint.insertMiniplayerFeatureFlagBooleanOverride(
                MINIPLAYER_DOUBLE_TAP_FEATURE_KEY,
                "enableMiniplayerDoubleTapAction",
            )
        }

        if (is_19_26_or_greater) {
            miniplayerModernConstructorFingerprint.method.apply {
                val literalIndex = indexOfFirstLiteralInstructionOrThrow(
                    MINIPLAYER_INITIAL_SIZE_FEATURE_KEY,
                )
                val targetIndex = indexOfFirstInstructionOrThrow(literalIndex, Opcode.LONG_TO_INT)

                val register = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstructions(
                    targetIndex + 1,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->setMiniplayerDefaultSize(I)I
                        move-result v$register
                    """,
                )
            }

            // Override a minimum size constant.
            miniplayerMinimumSizeFingerprint.method.apply {
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

        if (is_19_36_or_greater) {
            miniplayerModernConstructorFingerprint.insertMiniplayerFeatureFlagBooleanOverride(
                MINIPLAYER_ROUNDED_CORNERS_FEATURE_KEY,
                "setRoundedCorners",
            )
        }

        if (is_19_43_or_greater) {
            miniplayerOnCloseHandlerFingerprint.insertMiniplayerFeatureFlagBooleanOverride(
                MINIPLAYER_DISABLED_FEATURE_KEY,
                "getMiniplayerOnCloseHandler"
            )

            miniplayerModernConstructorFingerprint.insertMiniplayerFeatureFlagBooleanOverride(
                MINIPLAYER_HORIZONTAL_DRAG_FEATURE_KEY,
                "setHorizontalDrag",
            )
        }

        // endregion

        // region Fix 19.16 using mixed up drawables for tablet modern.
        // YT fixed this mistake in 19.17.
        // Fix this, by swapping the drawable resource values with each other.
        if (ytOutlinePictureInPictureWhite24 >= 0) {
            miniplayerModernExpandCloseDrawablesFingerprint.match(
                miniplayerModernViewParentFingerprint.originalClassDef,
            ).method.apply {
                listOf(
                    ytOutlinePictureInPictureWhite24 to ytOutlineXWhite24,
                    ytOutlineXWhite24 to ytOutlinePictureInPictureWhite24,
                ).forEach { (originalResource, replacementResource) ->
                    val imageResourceIndex = indexOfFirstLiteralInstructionOrThrow(originalResource)
                    val register = getInstruction<OneRegisterInstruction>(imageResourceIndex).registerA

                    replaceInstruction(imageResourceIndex, "const v$register, $replacementResource")
                }
            }
        }

        // endregion

        // region Add hooks to hide modern miniplayer buttons.

        listOf(
            Triple(
                miniplayerModernExpandButtonFingerprint,
                modernMiniplayerExpand,
                "hideMiniplayerExpandClose",
            ),
            Triple(
                miniplayerModernCloseButtonFingerprint,
                modernMiniplayerClose,
                "hideMiniplayerExpandClose",
            ),
            Triple(
                miniplayerModernRewindButtonFingerprint,
                modernMiniplayerRewindButton,
                "hideMiniplayerRewindForward",
            ),
            Triple(
                miniplayerModernForwardButtonFingerprint,
                modernMiniplayerForwardButton,
                "hideMiniplayerRewindForward",
            ),
            Triple(
                miniplayerModernOverlayViewFingerprint,
                scrimOverlay,
                "adjustMiniplayerOpacity",
            ),
        ).forEach { (fingerprint, literalValue, methodName) ->
            fingerprint.match(
                miniplayerModernViewParentFingerprint.classDef,
            ).method.hookInflatedView(
                literalValue,
                "Landroid/widget/ImageView;",
                "$EXTENSION_CLASS_DESCRIPTOR->$methodName(Landroid/widget/ImageView;)V",
            )
        }

        miniplayerModernAddViewListenerFingerprint.match(
            miniplayerModernViewParentFingerprint.classDef,
        ).method.addInstruction(
            0,
            "invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->" +
                "hideMiniplayerSubTexts(Landroid/view/View;)V",
        )

        // Modern 2 has a broken overlay subtitle view that is always present.
        // Modern 2 uses the same overlay controls as the regular video player,
        // and the overlay views are added at runtime.
        // Add a hook to the overlay class, and pass the added views to extension.
        //
        // NOTE: Modern 2 uses the same video UI as the regular player except resized to smaller.
        // This patch code could be used to hide other player overlays that do not use Litho.
        playerOverlaysLayoutFingerprint.classDef.methods.add(
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
                        invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->playerOverlayGroupCreated(Landroid/view/View;)V
                        return-void
                    """
                )
            }
        )

        // endregion
    }
}
