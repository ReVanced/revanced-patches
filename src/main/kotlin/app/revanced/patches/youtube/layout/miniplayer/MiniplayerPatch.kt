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
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference
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
import app.revanced.patches.youtube.layout.tablet.fingerprints.GetFormFactorFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.findOpcodeIndices
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstWideLiteralInstructionValueOrThrow
import app.revanced.util.patch.LiteralValueFingerprint
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

// YT uses "Miniplayer" without a space between 'mini' and 'player.
@Patch(
    name = "Miniplayer",
    description = "Adds options to change the in app minimized player, " +
            "and if patching 19.16+ also adds options to use modern miniplayers.",
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
        MiniplayerModernViewParentFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/MiniplayerPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        // Modern mini player is only present and functional in 19.15+
        // Of note, some modern miniplayer code is present in 19.14 but the feature is not complete.
        val isPatchingLegacy = ytOutlinePictureInPictureWhite24 < 0

        SettingsPatch.PreferenceScreen.PLAYER.addPreferences(
            PreferenceScreen(
                key = "revanced_miniplayer_screen",
                sorting = Sorting.UNSORTED,
                preferences =
                if (isPatchingLegacy) {
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
                        SwitchPreference("revanced_miniplayer_hide_sub_text"),
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
            findReturnIndices().forEach { index -> insertTabletOverride(index) }
        }

        // endregion


        // region Legacy pre 19.15 targets

        if (isPatchingLegacy) {
            MiniplayerOverrideFingerprint.resultOrThrow().let {
                val appNameStringIndex = it.scanResult.stringsScanResult!!.matches.first().index + 2

                it.mutableMethod.apply {
                    val walkerMethod = context.toMethodWalker(this)
                        .nextMethod(appNameStringIndex, true)
                        .getMethod() as MutableMethod

                    walkerMethod.apply {
                        findReturnIndices().forEach { index -> insertTabletOverride(index) }
                    }
                }
            }

            MiniplayerResponseModelSizeCheckFingerprint.resultOrThrow().let {
                it.mutableMethod.insertTabletOverride(it.scanResult.patternScanResult!!.endIndex)
            }

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

                    insertModernOverrideInt(iPutIndex)
                } else {
                    findReturnIndices().forEach { index -> insertModernOverride(index) }
                }
            }
        }

        // endregion

        // region Fix 19.16 using mixed up drawables for tablet modern.
        // YT fixed this mistake in 19.17
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
            MiniplayerModernExpandButtonFingerprint to "hideMiniplayerExpandClose",
            MiniplayerModernCloseButtonFingerprint to "hideMiniplayerExpandClose",
            MiniplayerModernRewindButtonFingerprint to "hideMiniplayerRewindForward",
            MiniplayerModernForwardButtonFingerprint to "hideMiniplayerRewindForward",
            MiniplayerModernOverlayViewFingerprint to "adjustMiniplayerOpacity",
        ).forEach { (fingerprint, methodName) ->
            fingerprint.addModernMiniplayerImageViewHook(context, methodName)
        }

        MiniplayerModernAddViewListenerFingerprint.apply {
            resolve(
                context,
                MiniplayerModernViewParentFingerprint.resultOrThrow().classDef
            )
        }.resultOrThrow().mutableMethod.apply {
            addInstruction(
                0,
                "invoke-static { p1 }, $INTEGRATIONS_CLASS_DESCRIPTOR->" +
                        "hideMiniplayerSubTexts(Landroid/view/View;)V"
            )
        }

        // endregion
    }

    private fun Method.findReturnIndices() = findOpcodeIndices(Opcode.RETURN)

    private fun MutableMethod.insertTabletOverride(index: Int) {
        insertModernTabletOverride(index, "getLegacyTabletOverride")
    }

    private fun MutableMethod.insertModernOverride(index: Int) {
        insertModernTabletOverride(index, "getModernOverride")
    }

    private fun MutableMethod.insertModernTabletOverride(index: Int, methodName: String) {
        val register = getInstruction<OneRegisterInstruction>(index).registerA
        this.addInstructions(
            index,
            """
                invoke-static {v$register}, $INTEGRATIONS_CLASS_DESCRIPTOR->$methodName(Z)Z
                move-result v$register
            """
        )
    }

    private fun MutableMethod.insertModernOverrideInt(iPutIndex: Int) {
        val targetInstruction = getInstruction<TwoRegisterInstruction>(iPutIndex)
        val targetReference = (targetInstruction as ReferenceInstruction).reference

        addInstructions(
            iPutIndex + 1, """
                invoke-static { v${targetInstruction.registerA} }, $INTEGRATIONS_CLASS_DESCRIPTOR->getModernOverrideType(I)I
                move-result v${targetInstruction.registerA}
                # Original instruction
                iput v${targetInstruction.registerA}, v${targetInstruction.registerB}, $targetReference 
            """
        )
        removeInstruction(iPutIndex)
    }

    private fun LiteralValueFingerprint.addModernMiniplayerImageViewHook(
        context: BytecodeContext,
        integrationsMethodName: String
    ) {
        resolve(
            context,
            MiniplayerModernViewParentFingerprint.resultOrThrow().classDef
        )

        resultOrThrow().mutableMethod.apply {
            val imageViewIndex = indexOfFirstInstructionOrThrow(
                indexOfFirstWideLiteralInstructionValueOrThrow(literalSupplier.invoke())
            ) {
                opcode == Opcode.CHECK_CAST
            }

            val register = getInstruction<OneRegisterInstruction>(imageViewIndex).registerA
            addInstruction(
                imageViewIndex + 1,
                "invoke-static { v$register }, $INTEGRATIONS_CLASS_DESCRIPTOR->$integrationsMethodName(Landroid/widget/ImageView;)V"
            )
        }
    }
}
