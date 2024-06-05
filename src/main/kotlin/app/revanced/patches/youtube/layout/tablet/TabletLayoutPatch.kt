package app.revanced.patches.youtube.layout.tablet

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.layout.tablet.TabletLayoutResourcePatch.ytOutlinePictureInPictureWhite24
import app.revanced.patches.youtube.layout.tablet.TabletLayoutResourcePatch.ytOutlineXWhite24
import app.revanced.patches.youtube.layout.tablet.fingerprints.GetFormFactorFingerprint
import app.revanced.patches.youtube.layout.tablet.fingerprints.MiniPlayerDimensionsCalculatorParentFingerprint
import app.revanced.patches.youtube.layout.tablet.fingerprints.MiniPlayerOverrideFingerprint
import app.revanced.patches.youtube.layout.tablet.fingerprints.MiniPlayerOverrideNoContextFingerprint
import app.revanced.patches.youtube.layout.tablet.fingerprints.MiniPlayerResponseModelSizeCheckFingerprint
import app.revanced.patches.youtube.layout.tablet.fingerprints.ModernMiniPlayerCloseButtonFingerprint
import app.revanced.patches.youtube.layout.tablet.fingerprints.ModernMiniPlayerConfigFingerprint
import app.revanced.patches.youtube.layout.tablet.fingerprints.ModernMiniPlayerConstructorFingerprint
import app.revanced.patches.youtube.layout.tablet.fingerprints.ModernMiniPlayerExpandButtonFingerprint
import app.revanced.patches.youtube.layout.tablet.fingerprints.ModernMiniPlayerExpandCloseDrawablesFingerprint
import app.revanced.patches.youtube.layout.tablet.fingerprints.ModernMiniPlayerForwardButtonFingerprint
import app.revanced.patches.youtube.layout.tablet.fingerprints.ModernMiniPlayerOverlayViewFingerprint
import app.revanced.patches.youtube.layout.tablet.fingerprints.ModernMiniPlayerRewindButtonFingerprint
import app.revanced.patches.youtube.layout.tablet.fingerprints.ModernMiniPlayerViewParentFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
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

@Patch(
    name = "Tablet layout",
    description = "Adds an option to force tablet layout and to use the tablet mini player",
    dependencies = [
        IntegrationsPatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class,
        TabletLayoutResourcePatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", arrayOf(
                // Hide modern mini player is only present in 19.15+
                // If a robust way of detecting the target app version is added,
                // then all changes except modern mini player could be applied to older versions.
                "19.15.36",
                "19.16.39",
            )
        )
    ]
)
@Suppress("unused")
object TabletLayoutPatch : BytecodePatch(
    setOf(GetFormFactorFingerprint,
        MiniPlayerDimensionsCalculatorParentFingerprint,
        MiniPlayerResponseModelSizeCheckFingerprint,
        MiniPlayerOverrideFingerprint,
        ModernMiniPlayerConfigFingerprint,
        ModernMiniPlayerConstructorFingerprint,
        ModernMiniPlayerViewParentFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/TabletLayoutPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            PreferenceScreen(
                key = "revanced_tablet_screen",
                sorting = PreferenceScreen.Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_tablet_layout"),
                    SwitchPreference("revanced_tablet_mini_player"),
                    SwitchPreference("revanced_tablet_mini_player_modern"),
                    SwitchPreference("revanced_tablet_mini_player_modern_hide_expand_close"),
                    SwitchPreference("revanced_tablet_mini_player_modern_hide_rewind_forward"),
                    TextPreference("revanced_tablet_mini_player_opacity", inputType = InputType.NUMBER)
                )
            )
        )

        // region Enable tablet mode.

        GetFormFactorFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                val returnIsLargeFormFactorIndex = getInstructions().lastIndex - 4
                val returnIsLargeFormFactorLabel = getInstruction(returnIsLargeFormFactorIndex)

                addInstructionsWithLabels(
                    0,
                    """
                          invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->getTabletLayoutEnabled()Z
                          move-result v0
                          if-nez v0, :is_large_form_factor
                    """,
                    ExternalLabel(
                        "is_large_form_factor",
                        returnIsLargeFormFactorLabel
                    )
                )
            }
        }

        // endregion


        // region Enable tablet mini player.

        MiniPlayerOverrideNoContextFingerprint.resolve(
            context,
            MiniPlayerDimensionsCalculatorParentFingerprint.resultOrThrow().classDef
        )
        MiniPlayerOverrideNoContextFingerprint.resultOrThrow().mutableMethod.apply {
            findReturnIndexes().forEach { index -> insertTabletOverride(index) }
        }

        // endregion


        // region Pre 19.15 patches.
        // These are not required for 19.15+.

        MiniPlayerOverrideFingerprint.resultOrThrow().let {
            val appNameStringIndex = it.scanResult.stringsScanResult!!.matches.first().index + 2

            it.mutableMethod.apply {
                val walkerMethod = context.toMethodWalker(this)
                    .nextMethod(appNameStringIndex, true)
                    .getMethod() as MutableMethod

                walkerMethod.apply {
                    findReturnIndexes().forEach { index -> insertTabletOverride(index) }
                }
            }
        }

        MiniPlayerResponseModelSizeCheckFingerprint.resultOrThrow().let {
            it.mutableMethod.insertTabletOverride(it.scanResult.patternScanResult!!.endIndex)
        }

        ModernMiniPlayerConfigFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                insertModernTabletOverrideBoolean(it.scanResult.patternScanResult!!.endIndex)
            }
        }

        // endregion


        // region Enable modern mini player.

        ModernMiniPlayerConstructorFingerprint.resultOrThrow().mutableClass.methods.forEach {
            it.apply {
                if (AccessFlags.CONSTRUCTOR.isSet(accessFlags)) {
                    val iPutIndex = indexOfFirstInstructionOrThrow {
                        this.opcode == Opcode.IPUT && this.getReference<FieldReference>()?.type == "I"
                    }

                    insertModernTabletOverrideInt(iPutIndex)
                } else {
                    findReturnIndexes().forEach { index -> insertModernTabletOverrideBoolean(index) }
                }
            }
        }

        // endregion


        // region Fix YT 19.15 and 19.16 using mixed up drawables for tablet modern mini player.

        ModernMiniPlayerExpandCloseDrawablesFingerprint.resolve(
            context,
            ModernMiniPlayerViewParentFingerprint.resultOrThrow().classDef
        )

        ModernMiniPlayerExpandCloseDrawablesFingerprint.resultOrThrow().mutableMethod.apply {
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


        // region Hide modern mini player buttons.

        ModernMiniPlayerOverlayViewFingerprint.addModernTabletMiniPlayerImageViewHook(
            context,
            "adjustModernTabletMiniPlayerOpacity"
        )

        ModernMiniPlayerExpandButtonFingerprint.addModernTabletMiniPlayerImageViewHook(
            context,
            "hideModernMiniPlayerExpandClose"
        )

        ModernMiniPlayerCloseButtonFingerprint.addModernTabletMiniPlayerImageViewHook(
            context,
            "hideModernMiniPlayerExpandClose"
        )

        ModernMiniPlayerRewindButtonFingerprint.addModernTabletMiniPlayerImageViewHook(
            context,
            "hideModernMiniPlayerRewindForward"
        )

        ModernMiniPlayerForwardButtonFingerprint.addModernTabletMiniPlayerImageViewHook(
            context,
            "hideModernMiniPlayerRewindForward"
        )

        // endregion
    }

    private fun Method.findReturnIndexes(): List<Int> {
        val indexes = implementation!!.instructions
            .withIndex()
            .filter { (_, instruction) -> instruction.opcode == Opcode.RETURN }
            .map { (index, _) -> index }
            .reversed()
        if (indexes.isEmpty()) throw PatchException("No return instructions found.")

        return indexes
    }

    private fun MutableMethod.insertTabletOverride(index: Int) {
        insertModernTabletOverride(index, "getTabletMiniPlayerOverride")
    }

    private fun MutableMethod.insertModernTabletOverrideBoolean(index: Int) {
        insertModernTabletOverride(index, "getModernTabletMiniPlayerOverrideBoolean")
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

    private fun MutableMethod.insertModernTabletOverrideInt(iPutIndex: Int) {
        val targetInstruction = getInstruction<TwoRegisterInstruction>(iPutIndex)
        val targetReference = (targetInstruction as ReferenceInstruction).reference

        addInstructions(
            iPutIndex + 1, """
                invoke-static { v${targetInstruction.registerA} }, $INTEGRATIONS_CLASS_DESCRIPTOR->getModernTabletMiniPlayerOverrideInt(I)I
                move-result v${targetInstruction.registerA}
                # Original instruction
                iput v${targetInstruction.registerA}, v${targetInstruction.registerB}, $targetReference 
            """
        )
        removeInstruction(iPutIndex)
    }

    private fun LiteralValueFingerprint.addModernTabletMiniPlayerImageViewHook(
        context: BytecodeContext,
        integrationsMethodName: String
    ) {
        resolve(
            context,
            ModernMiniPlayerViewParentFingerprint.resultOrThrow().classDef
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
