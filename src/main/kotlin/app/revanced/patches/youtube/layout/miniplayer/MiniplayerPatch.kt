package app.revanced.patches.youtube.layout.miniplayer

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
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
    description = "Adds options to change the in app minimized player",
    dependencies = [
        IntegrationsPatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class,
        MiniplayerResourcePatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                // Hide modern miniplayer is only present in 19.15+
                // If a robust way of detecting the target app version is added,
                // then all changes except modern miniplayer could be applied to older versions.
                "19.15.36",
                "19.16.39"
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

        SettingsPatch.PreferenceScreen.PLAYER.addPreferences(
            PreferenceScreen(
                key = "revanced_miniplayer_screen",
                sorting = Sorting.UNSORTED,
                preferences = setOf(
                    ListPreference(key = "revanced_miniplayer_type", summaryKey = null,),
                    SwitchPreference("revanced_miniplayer_hide_expand_close"),
                    SwitchPreference("revanced_miniplayer_hide_sub_text"),
                    SwitchPreference("revanced_miniplayer_hide_rewind_forward"),
                    TextPreference("revanced_miniplayer_opacity", inputType = InputType.NUMBER)
                )
            )
        )

        // region Enable tablet miniplayer.

        MiniplayerOverrideNoContextFingerprint.resolve(
            context,
            MiniplayerDimensionsCalculatorParentFingerprint.resultOrThrow().classDef
        )
        MiniplayerOverrideNoContextFingerprint.resultOrThrow().mutableMethod.apply {
            findReturnIndexes().forEach { index -> insertTabletOverride(index) }
        }

        // endregion


        // region Pre 19.15 patches.
        // These are not required for 19.15+.

        MiniplayerOverrideFingerprint.resultOrThrow().let {
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

        MiniplayerResponseModelSizeCheckFingerprint.resultOrThrow().let {
            it.mutableMethod.insertTabletOverride(it.scanResult.patternScanResult!!.endIndex)
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
                    findReturnIndexes().forEach { index -> insertModernOverride(index) }
                }
            }
        }

        // endregion


        // region Fix YT 19.15 and 19.16 using mixed up drawables for tablet modern.

        MiniplayerModernExpandCloseDrawablesFingerprint.let {
            it.resolve(
                context,
                MiniplayerModernViewParentFingerprint.resultOrThrow().classDef
            )

            it.resultOrThrow().mutableMethod.apply {
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


        // region Hide tablet modern miniplayer buttons.

        MiniplayerModernExpandButtonFingerprint.addModernMiniplayerImageViewHook(
            context,
            "hideMiniplayerExpandClose"
        )

        MiniplayerModernCloseButtonFingerprint.addModernMiniplayerImageViewHook(
            context,
            "hideMiniplayerExpandClose"
        )

        MiniplayerModernRewindButtonFingerprint.addModernMiniplayerImageViewHook(
            context,
            "hideMiniplayerRewindForward"
        )

        MiniplayerModernForwardButtonFingerprint.addModernMiniplayerImageViewHook(
            context,
            "hideMiniplayerRewindForward"
        )

        MiniplayerModernOverlayViewFingerprint.addModernMiniplayerImageViewHook(
            context,
            "adjustMiniplayerOpacity"
        )

        MiniplayerModernAddViewListenerFingerprint.let {
            it.resolve(
                context,
                MiniplayerModernViewParentFingerprint.resultOrThrow().classDef
            )

            it.resultOrThrow().mutableMethod.apply {
                addInstruction(
                    0,
                    "invoke-static { p1 }, $INTEGRATIONS_CLASS_DESCRIPTOR->" +
                            "hideMiniplayerSubTexts(Landroid/view/View;)V"
                )
            }
        }

        // endregion
    }

    private fun Method.findReturnIndexes(): List<Int> {
        val indexes = implementation!!.instructions
            .withIndex()
            .filter { (_, instruction) -> instruction.opcode == Opcode.RETURN }
            .map { (index, _) -> index }
            .reversed()
        if (indexes.isEmpty()) throw PatchException("No return instructions found in: $this")

        return indexes
    }

    private fun MutableMethod.insertTabletOverride(index: Int) {
        insertModernTabletOverride(index, "getTabletOverride")
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
