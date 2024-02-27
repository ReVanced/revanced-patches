package app.revanced.patches.youtube.utils.navbarindex

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.utils.integrations.Constants.UTILS_PATH
import app.revanced.patches.youtube.utils.navbarindex.fingerprints.MobileTopBarButtonOnClickFingerprint
import app.revanced.patches.youtube.utils.navbarindex.fingerprints.PivotBarIndexFingerprint
import app.revanced.patches.youtube.utils.navbarindex.fingerprints.SettingsActivityOnBackPressedFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

@Suppress("unused")
object NavBarIndexHookPatch : BytecodePatch(
    setOf(
        MobileTopBarButtonOnClickFingerprint,
        PivotBarIndexFingerprint,
        SettingsActivityOnBackPressedFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "$UTILS_PATH/NavBarIndexPatch;"

    override fun execute(context: BytecodeContext) {

        /**
         * Change NavBar Index value according to selected Tab.
         */
        PivotBarIndexFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.startIndex + 1
                val booleanRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerD
                val freeRegister = implementation!!.registerCount - parameters.size - 2

                addInstruction(
                    insertIndex,
                    "invoke-static {v$freeRegister, v$booleanRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->setNavBarIndex(IZ)V"
                )
                addInstruction(
                    0,
                    "move/16 v$freeRegister, p1"
                )
            }
        } ?: throw PivotBarIndexFingerprint.exception

        /**
         * Since it is used only after opening the library tab, set index to 4.
         */
        arrayOf(
            MobileTopBarButtonOnClickFingerprint,
            SettingsActivityOnBackPressedFingerprint
        ).forEach { fingerprint ->
            fingerprint.injectIndex()
        }
    }

    private fun MethodFingerprint.injectIndex() {
        result?.let {
            it.mutableMethod.apply {
                addInstructions(
                    0, """
                        const/4 v0, 0x4
                        invoke-static {v0}, $INTEGRATIONS_CLASS_DESCRIPTOR->setNavBarIndex(I)V
                        """
                )
            }
        } ?: throw exception
    }
}