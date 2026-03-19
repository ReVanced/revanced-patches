@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.youtube.layout.miniplayer

import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod
import app.revanced.patcher.classDef
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.*
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.*
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.*
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal var ytOutlinePictureInPictureWhite24 = -1L
    private set

private val miniplayerResourcePatch = resourcePatch {
    dependsOn(
        resourceMappingPatch,
        versionCheckPatch,
    )

    apply {
        // Resource id is not used during patching, but is used by extension.
        // Verify the resource is present while patching.
        ResourceType.ID["modern_miniplayer_subtitle_text"]
    }
}

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/MiniplayerPatch;"

@Suppress("unused")
val miniplayerPatch = bytecodePatch(
    name = "Miniplayer",
    description = "Adds options to change the in-app minimized player.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        miniplayerResourcePatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "20.14.43",
            "20.21.37",
            "20.26.46",
            "20.31.42",
            "20.37.48",
            "20.40.45"
        ),
    )

    apply {
        addResources("youtube", "layout.miniplayer.miniplayerPatch")

        val preferences = mutableSetOf<BasePreference>()

        preferences +=
            if (is_20_37_or_greater) {
                ListPreference("revanced_miniplayer_type")
            } else {
                ListPreference(
                    key = "revanced_miniplayer_type",
                    entriesKey = "revanced_miniplayer_type_legacy_20_03_entries",
                    entryValuesKey = "revanced_miniplayer_type_legacy_20_03_entry_values",
                )
            }

        preferences += SwitchPreference("revanced_miniplayer_disable_drag_and_drop")
        preferences += SwitchPreference("revanced_miniplayer_disable_horizontal_drag")
        preferences += SwitchPreference("revanced_miniplayer_disable_rounded_corners")
        preferences += SwitchPreference("revanced_miniplayer_hide_subtext")
        preferences += SwitchPreference("revanced_miniplayer_hide_overlay_buttons")

        preferences += TextPreference(
            "revanced_miniplayer_width_dip",
            inputType = InputType.NUMBER
        )

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
                """,
            )
        }

        fun Method.findReturnIndicesReversed() =
            findInstructionIndicesReversedOrThrow(Opcode.RETURN)

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

        fun MutableMethod.insertMiniplayerFeatureFlagBooleanOverride(
            literal: Long,
            extensionMethod: String,
        ) = insertLiteralOverride(
            literal,
            "$EXTENSION_CLASS_DESCRIPTOR->$extensionMethod(Z)Z",
        )

        fun MutableMethod.insertMiniplayerFeatureFlagFloatOverride(
            literal: Long,
            extensionMethod: String,
        ) = apply {
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

        // region Enable tablet miniplayer.
        // Parts of the YT code is removed in 20.37+ and the legacy player no longer works.

        if (!is_20_37_or_greater) {
            miniplayerDimensionsCalculatorParentMethod.immutableClassDef.getMiniplayerOverrideNoContextMethod()
                .apply {
                    findReturnIndicesReversed().forEach { index ->
                        insertLegacyTabletMiniplayerOverride(index)
                    }
                }

            // endregion

            // region Legacy tablet miniplayer hooks.
            miniplayerOverrideMethodMatch.let {
                val appNameStringIndex = it[-1]
                navigate(it.immutableMethod).to(appNameStringIndex).stop().apply {
                    findReturnIndicesReversed().forEach { index ->
                        insertLegacyTabletMiniplayerOverride(
                            index,
                        )
                    }
                }
            }

            miniplayerResponseModelSizeCheckMethodMatch.let {
                it.method.insertLegacyTabletMiniplayerOverride(it[-1])
            }
        }

        // endregion

        // region Enable modern miniplayer.

        miniplayerModernConstructorMethod.classDef.methods.forEach {
            it.apply {
                if (AccessFlags.CONSTRUCTOR.isSet(accessFlags)) {
                    val iPutIndex = indexOfFirstInstructionOrThrow {
                        this.opcode == Opcode.IPUT && this.getReference<FieldReference>()?.type == "I"
                    }

                    insertModernMiniplayerTypeOverride(iPutIndex)
                } else {
                    findReturnIndicesReversed().forEach { index ->
                        insertModernMiniplayerOverride(
                            index
                        )
                    }
                }
            }
        }

        miniplayerModernConstructorMethod.insertMiniplayerFeatureFlagBooleanOverride(
            MINIPLAYER_DRAG_DROP_FEATURE_KEY,
            "getMiniplayerDragAndDrop",
        )

        miniplayerModernConstructorMethod.insertMiniplayerFeatureFlagBooleanOverride(
            MINIPLAYER_MODERN_FEATURE_LEGACY_KEY,
            "getModernMiniplayerOverride",
        )

        miniplayerModernConstructorMethod.insertMiniplayerFeatureFlagBooleanOverride(
            MINIPLAYER_MODERN_FEATURE_KEY,
            "getModernFeatureFlagsActiveOverride",
        )

        miniplayerModernConstructorMethod.insertMiniplayerFeatureFlagBooleanOverride(
            MINIPLAYER_DOUBLE_TAP_FEATURE_KEY,
            "getMiniplayerDoubleTapAction",
        )

        miniplayerModernConstructorMethod.apply {
            val literalIndex = indexOfFirstLiteralInstructionOrThrow(
                MINIPLAYER_INITIAL_SIZE_FEATURE_KEY,
            )
            val targetIndex = indexOfFirstInstructionOrThrow(literalIndex, Opcode.LONG_TO_INT)
            val register = getInstruction<OneRegisterInstruction>(targetIndex).registerA

            addInstructions(
                targetIndex + 1,
                """
                    invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getMiniplayerDefaultSize(I)I
                    move-result v$register
                """,
            )
        }

        // Override a minimum size constant.
        miniplayerMinimumSizeMethodMatch.let {
            it.method.apply {
                val index = it[1]
                val register = getInstruction<OneRegisterInstruction>(index).registerA

                // Smaller sizes can be used, but the miniplayer will always start in size 170 if set any smaller.
                // The 170 initial limit probably could be patched to allow even smaller initial sizes,
                // but 170 is already half the horizontal space and smaller does not seem useful.
                replaceInstruction(index, "const/16 v$register, 170")
            }
        }

        miniplayerModernConstructorMethod.insertMiniplayerFeatureFlagBooleanOverride(
            MINIPLAYER_ROUNDED_CORNERS_FEATURE_KEY,
            "getRoundedCorners",
        )

        miniplayerOnCloseHandlerMethod.insertMiniplayerFeatureFlagBooleanOverride(
            MINIPLAYER_DISABLED_FEATURE_KEY,
            "getMiniplayerOnCloseHandler",
        )

        miniplayerModernConstructorMethod.insertMiniplayerFeatureFlagBooleanOverride(
            MINIPLAYER_HORIZONTAL_DRAG_FEATURE_KEY,
            "getHorizontalDrag",
        )

        miniplayerModernConstructorMethod.insertMiniplayerFeatureFlagBooleanOverride(
            MINIPLAYER_ANIMATED_EXPAND_FEATURE_KEY,
            "getMaximizeAnimation",
        )

        // endregion

        // region fix minimal miniplayer using the wrong pause/play bold icons.

        if (is_20_31_or_greater) {
            miniplayerSetIconsMethod.apply {
                findInstructionIndicesReversedOrThrow {
                    val reference = getReference<MethodReference>()
                    opcode == Opcode.INVOKE_INTERFACE &&
                            reference?.returnType == "Z" && reference.parameterTypes.isEmpty()
                }.forEach { index ->
                    val register = getInstruction<OneRegisterInstruction>(index + 1).registerA

                    addInstructions(
                        index + 2,
                        """
                            invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->allowBoldIcons(Z)Z
                            move-result v$register
                        """,
                    )
                }
            }
        }

        // endregion

        // region Add hooks to hide modern miniplayer buttons.

        listOf(
            ClassDef::miniplayerModernExpandButtonMethodMatch to "hideMiniplayerExpandClose",
            ClassDef::miniplayerModernCloseButtonMethodMatch to "hideMiniplayerExpandClose",
            ClassDef::miniplayerModernActionButtonMethodMatch to "hideMiniplayerActionButton",
            ClassDef::miniplayerModernRewindButtonMethodMatch to "hideMiniplayerRewindForward",
            ClassDef::miniplayerModernForwardButtonMethodMatch to "hideMiniplayerRewindForward",
            ClassDef::miniplayerModernOverlayViewMethodMatch to "adjustMiniplayerOpacity",
        ).forEach { (matching, methodName) ->
            val match = matching.get(miniplayerModernViewParentMethod.immutableClassDef)

            match.method.apply {
                val index = match[-1]
                val register = getInstruction<OneRegisterInstruction>(index).registerA

                addInstruction(
                    index + 1,
                    "invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->$methodName(Landroid/view/View;)V",
                )
            }
        }

        miniplayerModernViewParentMethod.immutableClassDef.getMiniplayerModernAddViewListenerMethod()
            .addInstruction(
                0,
                "invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->" +
                        "hideMiniplayerSubTexts(Landroid/view/View;)V",
            )

        // endregion
    }
}
