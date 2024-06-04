package app.revanced.patches.youtube.layout.tabletminiplayer

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.tabletminiplayer.fingerprints.MiniPlayerDimensionsCalculatorParentFingerprint
import app.revanced.patches.youtube.layout.tabletminiplayer.fingerprints.MiniPlayerOverrideFingerprint
import app.revanced.patches.youtube.layout.tabletminiplayer.fingerprints.MiniPlayerOverrideNoContextFingerprint
import app.revanced.patches.youtube.layout.tabletminiplayer.fingerprints.MiniPlayerResponseModelSizeCheckFingerprint
import app.revanced.patches.youtube.layout.tabletminiplayer.fingerprints.ModernMiniPlayerCloseImageViewFingerprint
import app.revanced.patches.youtube.layout.tabletminiplayer.fingerprints.ModernMiniPlayerConfigFingerprint
import app.revanced.patches.youtube.layout.tabletminiplayer.fingerprints.ModernMiniPlayerConstructorFingerprint
import app.revanced.patches.youtube.layout.tabletminiplayer.fingerprints.ModernMiniPlayerExpandImageViewFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstWideLiteralInstructionValueOrThrow
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Patch(
    name = "Tablet mini player",
    description = "Adds an option to enable the tablet mini player layout.",
    dependencies = [
        IntegrationsPatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class,
        TabletMiniPlayerResourcePatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", arrayOf(
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
                "19.14.43",
                "19.15.36",
                "19.16.39",
            )
        )
    ]
)
@Suppress("unused")
object TabletMiniPlayerPatch : BytecodePatch(
    setOf(
        MiniPlayerDimensionsCalculatorParentFingerprint,
        MiniPlayerResponseModelSizeCheckFingerprint,
        MiniPlayerOverrideFingerprint,
        ModernMiniPlayerConfigFingerprint,
        ModernMiniPlayerConstructorFingerprint,
        ModernMiniPlayerExpandImageViewFingerprint,
        ModernMiniPlayerCloseImageViewFingerprint,
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/TabletMiniPlayerOverridePatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_tablet_mini_player"),
            SwitchPreference("revanced_tablet_mini_player_modern"),
            SwitchPreference("revanced_tablet_mini_player_modern_hide_expand_close")
        )

        // Override every return instruction with the proxy call.
        MiniPlayerOverrideNoContextFingerprint.resolve(
            context,
            MiniPlayerDimensionsCalculatorParentFingerprint.resultOrThrow().classDef
        )
        MiniPlayerOverrideNoContextFingerprint.resultOrThrow().mutableMethod.apply {
            findReturnIndexes().forEach { index -> insertTabletOverride(index) }
        }

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

        // Size check return value override.
        MiniPlayerResponseModelSizeCheckFingerprint.resultOrThrow().let {
            it.mutableMethod.insertTabletOverride(it.scanResult.patternScanResult!!.endIndex)
        }

        // Enable modern mini player
        ModernMiniPlayerConfigFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                insertModernTabletOverrideBoolean(it.scanResult.patternScanResult!!.endIndex)
            }
        }

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

        // Hide modern mini player buttons
        listOf(
            ModernMiniPlayerExpandImageViewFingerprint to TabletMiniPlayerResourcePatch.modernMiniplayerExpand,
            ModernMiniPlayerCloseImageViewFingerprint to TabletMiniPlayerResourcePatch.modernMiniplayerClose
        ).forEach { (fingerprint, resourceId) ->
            fingerprint.resultOrThrow().mutableMethod.apply {
                val imageViewIndex = indexOfFirstInstructionOrThrow(
                    indexOfFirstWideLiteralInstructionValueOrThrow(resourceId)
                ) {
                    opcode == Opcode.MOVE_RESULT_OBJECT
                }

                val register = getInstruction<OneRegisterInstruction>(imageViewIndex).registerA
                addInstruction(imageViewIndex + 1,
                    "invoke-static { v$register }, $INTEGRATIONS_CLASS_DESCRIPTOR->hideModernMiniPlayerButtonView(Landroid/view/View;)V")
            }
        }
    }

    private fun Method.findReturnIndexes(): List<Int> {
        val indexes = implementation!!.instructions
            .withIndex()
            .filter { (_, instruction) -> instruction.opcode == Opcode.RETURN }
            .map { (index, _) -> index }
            .reversed()
        if (indexes.isEmpty()) throw PatchException("No return instructions found.")

        return indexes;
    }

    private fun MutableMethod.insertTabletOverride(index: Int) {
        insertOverride(index, "getTabletMiniPlayerOverride")
    }

    private fun MutableMethod.insertModernTabletOverrideBoolean(index: Int) {
        insertOverride(index, "getModernTabletMiniPlayerOverrideBoolean")
    }

    private fun MutableMethod.insertOverride(index: Int, methodName: String) {
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
                invoke-static {v${targetInstruction.registerA}}, $INTEGRATIONS_CLASS_DESCRIPTOR->getModernTabletMiniPlayerOverrideInt(I)I
                move-result v${targetInstruction.registerA}
                iput v${targetInstruction.registerA}, v${targetInstruction.registerB}, $targetReference
            """
        )
        removeInstruction(iPutIndex)
    }
}
