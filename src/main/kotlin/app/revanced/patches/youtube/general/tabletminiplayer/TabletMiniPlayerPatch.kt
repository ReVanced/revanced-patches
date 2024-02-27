package app.revanced.patches.youtube.general.tabletminiplayer

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.general.tabletminiplayer.fingerprints.MiniPlayerDimensionsCalculatorFingerprint
import app.revanced.patches.youtube.general.tabletminiplayer.fingerprints.MiniPlayerOverrideFingerprint
import app.revanced.patches.youtube.general.tabletminiplayer.fingerprints.MiniPlayerOverrideNoContextFingerprint
import app.revanced.patches.youtube.general.tabletminiplayer.fingerprints.MiniPlayerResponseModelSizeCheckFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.GENERAL
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.getStringInstructionIndex
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Enable tablet mini player",
    description = "Adds an option to enable the tablet mini player layout.",
    dependencies = [
        SettingsPatch::class,
        SharedResourceIdPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object TabletMiniPlayerPatch : BytecodePatch(
    setOf(
        MiniPlayerDimensionsCalculatorFingerprint,
        MiniPlayerResponseModelSizeCheckFingerprint,
        MiniPlayerOverrideFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        MiniPlayerDimensionsCalculatorFingerprint.result?.let { parentResult ->
            MiniPlayerOverrideNoContextFingerprint.also {
                it.resolve(
                    context,
                    parentResult.classDef
                )
            }.result?.let { result ->
                val (method, _, parameterRegister) = result.addProxyCall()
                method.insertOverride(
                    method.implementation!!.instructions.size - 1,
                    parameterRegister
                )
            } ?: throw MiniPlayerOverrideNoContextFingerprint.exception
        } ?: throw MiniPlayerDimensionsCalculatorFingerprint.exception

        MiniPlayerOverrideFingerprint.result?.let {
            it.mutableMethod.apply {
                (context.toMethodWalker(this)
                    .nextMethod(getStringInstructionIndex("appName") + 2, true)
                    .getMethod() as MutableMethod)
                    .instructionProxyCall()
            }
        } ?: throw MiniPlayerOverrideFingerprint.exception

        MiniPlayerResponseModelSizeCheckFingerprint.result?.addProxyCall()
            ?: throw MiniPlayerResponseModelSizeCheckFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: GENERAL_SETTINGS",
                "SETTINGS: ENABLE_TABLET_MINI_PLAYER"
            )
        )

        SettingsPatch.updatePatchStatus("Enable tablet mini player")

    }

    // helper methods
    private fun MethodFingerprintResult.addProxyCall(): Triple<MutableMethod, Int, Int> {
        val (method, scanIndex, parameterRegister) = this.unwrap()
        method.insertOverride(scanIndex, parameterRegister)

        return Triple(method, scanIndex, parameterRegister)
    }

    private fun MutableMethod.insertOverride(index: Int, overrideRegister: Int) {
        this.addInstructions(
            index, """
                invoke-static {v$overrideRegister}, $GENERAL->enableTabletMiniPlayer(Z)Z
                move-result v$overrideRegister
                """
        )
    }

    private fun MutableMethod.instructionProxyCall() {
        val insertInstructions = this.implementation!!.instructions
        for ((index, instruction) in insertInstructions.withIndex()) {
            if (instruction.opcode != Opcode.RETURN) continue
            val parameterRegister = this.getInstruction<OneRegisterInstruction>(index).registerA
            this.insertOverride(index, parameterRegister)
            this.insertOverride(insertInstructions.size - 1, parameterRegister)
            break
        }
    }

    private fun MethodFingerprintResult.unwrap(): Triple<MutableMethod, Int, Int> {
        val scanIndex = this.scanResult.patternScanResult!!.endIndex
        val method = this.mutableMethod
        val parameterRegister =
            method.getInstruction<OneRegisterInstruction>(scanIndex).registerA

        return Triple(method, scanIndex, parameterRegister)
    }
}
