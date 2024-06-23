package app.revanced.patches.youtube.layout.miniplayer

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.all.misc.resources.AddResourcesPatch
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
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.findOpcodeIndicesReversed
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstWideLiteralInstructionValueOrThrow
import app.revanced.util.patch.LiteralValueFingerprint
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

// YT uses "Miniplayer" without a space between 'mini' and 'player: https://support.google.com/youtube/answer/9162927.
@Patch(
    name = "Miniplayer",
    description = "Adds options to change the in app minimized player, " +
            "and if patching target 19.16+ adds options to use modern miniplayers.",
    dependencies = [
        IntegrationsPatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class,
        MiniplayerResourcePatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39",
                "19.03.36",
                "19.04.38",
                "19.05.36",
                "19.06.39",
                "19.07.40",
                "19.08.36",
                "19.09.38",
                "19.10.39",
                "19.11.43",
                "19.12.41",
                "19.13.37",
                // 19.14 is left out, as it has incomplete miniplayer code and missing some UI resources.
                // It's simpler to not bother with supporting this single old version.
                // 19.15 has a different code for handling sub title texts,
                // and also probably not worth making changes just to support this single old version.
                "19.16.39" // Earliest supported version with modern miniplayers.
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
        YouTubePlayerOverlaysLayoutFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/MiniplayerPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        // Modern mini player is only present and functional in 19.15+.
        // Resource is not present in older versions. Using it to determine, if patching an old version.
        val isPatchingOldVersion = ytOutlinePictureInPictureWhite24 < 0

        SettingsPatch.PreferenceScreen.PLAYER.addPreferences(
            PreferenceScreen(
                key = "revanced_miniplayer_screen",
                sorting = Sorting.UNSORTED,
                preferences =
                if (isPatchingOldVersion) {
                    setOf(
                        ListPreference(
                            "revanced_miniplayer_type",
                            summaryKey = null,
                            entriesKey = "revanced_miniplayer_type_legacy_entries",
                            entryValuesKey = "revanced_miniplayer_type_legacy_entry_values"
                        )
                    )
                } else {
                    setOf(
                        ListPreference(
                            "revanced_miniplayer_type",
                            summaryKey = null,
                            entriesKey = "revanced_miniplayer_type_19_15_entries",
                            entryValuesKey = "revanced_miniplayer_type_19_15_entry_values"
                        ),
                        SwitchPreference("revanced_miniplayer_hide_expand_close"),
                        SwitchPreference("revanced_miniplayer_hide_subtext"),
                        SwitchPreference("revanced_miniplayer_hide_rewind_forward"),
                        TextPreference("revanced_miniplayer_opacity", inputType = InputType.NUMBER)
                    )
                }
            )
        )

        // region Enable tablet miniplayer.

        MiniplayerOverrideNoContextFingerprint.resolve(
            context,
            MiniplayerDimensionsCalculatorParentFingerprint.resultOrThrow().classDef
        )
        MiniplayerOverrideNoContextFingerprint.resultOrThrow().mutableMethod.apply {
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

        if (isPatchingOldVersion) {
            // Return here, as patch below is only intended for new versions of the app.
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

        // endregion

        // region Fix 19.16 using mixed up drawables for tablet modern.
        // YT fixed this mistake in 19.17.
        // Fix this, by swapping the drawable resource values with each other.

        MiniplayerModernExpandCloseDrawablesFingerprint.apply {
            resolve(
                context,
                MiniplayerModernViewParentFingerprint.resultOrThrow().classDef
            )
        }.resultOrThrow().mutableMethod.apply {
            listOf(
                ytOutlinePictureInPictureWhite24 to ytOutlineXWhite24,
                ytOutlineXWhite24 to ytOutlinePictureInPictureWhite24,
            ).forEach { (originalResource, replacementResource) ->
                val imageResourceIndex = indexOfFirstWideLiteralInstructionValueOrThrow(originalResource)
                val register = getInstruction<OneRegisterInstruction>(imageResourceIndex).registerA

                replaceInstruction(imageResourceIndex, "const v$register, $replacementResource")
            }
        }

        // endregion


        // region Add hooks to hide tablet modern miniplayer buttons.

        listOf(
            Triple(MiniplayerModernExpandButtonFingerprint, modernMiniplayerExpand,"hideMiniplayerExpandClose"),
            Triple(MiniplayerModernCloseButtonFingerprint, modernMiniplayerClose, "hideMiniplayerExpandClose"),
            Triple(MiniplayerModernRewindButtonFingerprint, modernMiniplayerRewindButton, "hideMiniplayerRewindForward"),
            Triple(MiniplayerModernForwardButtonFingerprint, modernMiniplayerForwardButton, "hideMiniplayerRewindForward"),
            Triple(MiniplayerModernOverlayViewFingerprint, scrimOverlay, "adjustMiniplayerOpacity")
        ).forEach { (fingerprint, literalValue, methodName) ->
            fingerprint.resolve(
                context,
                MiniplayerModernViewParentFingerprint.resultOrThrow().classDef
            )

            fingerprint.hookInflatedView(
                literalValue,
                "Landroid/widget/ImageView;",
                "$INTEGRATIONS_CLASS_DESCRIPTOR->$methodName(Landroid/widget/ImageView;)V"
            )
        }

        MiniplayerModernAddViewListenerFingerprint.apply {
            resolve(
                context,
                MiniplayerModernViewParentFingerprint.resultOrThrow().classDef
            )
        }.resultOrThrow().mutableMethod.addInstruction(
            0,
            "invoke-static { p1 }, $INTEGRATIONS_CLASS_DESCRIPTOR->" +
                    "hideMiniplayerSubTexts(Landroid/view/View;)V"
        )


        // Modern 2 has a broken overlay subtitle view that is always present.
        // Modern 2 uses the same overlay controls as the regular video player,
        // and the overlay views are added at runtime.
        // Add a hook to the overlay class, and pass the added views to integrations.
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
        val targetReference = (targetInstruction as ReferenceInstruction).reference

        addInstructions(
            iPutIndex + 1, """
                invoke-static { v${targetInstruction.registerA} }, $INTEGRATIONS_CLASS_DESCRIPTOR->getModernMiniplayerOverrideType(I)I
                move-result v${targetInstruction.registerA}
                # Original instruction
                iput v${targetInstruction.registerA}, v${targetInstruction.registerB}, $targetReference 
            """
        )
        removeInstruction(iPutIndex)
    }

    private fun LiteralValueFingerprint.hookInflatedView(
        literalValue: Long,
        hookedClassType: String,
        integrationsMethodName: String,
    ) {
        resultOrThrow().mutableMethod.apply {
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
}
