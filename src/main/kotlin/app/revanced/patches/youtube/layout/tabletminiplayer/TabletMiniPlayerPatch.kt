package app.revanced.patches.youtube.layout.tabletminiplayer

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint.MethodFingerprint
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
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Tablet mini player",
    description = "Adds an option to enable the tablet mini player layout.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class, AddResourcesPatch::class],
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
                "19.11.43"
            )
        )
    ]
)
@Suppress("unused")
object TabletMiniPlayerPatch : BytecodePatch(
    setOf(
        MiniPlayerDimensionsCalculatorParentFingerprint,
        MiniPlayerResponseModelSizeCheckFingerprint,
        MiniPlayerOverrideFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_tablet_miniplayer")
        )

        // First resolve the fingerprints via the parent fingerprint.
        MiniPlayerDimensionsCalculatorParentFingerprint.result
            ?: throw MiniPlayerDimensionsCalculatorParentFingerprint.exception
        val miniPlayerClass = MiniPlayerDimensionsCalculatorParentFingerprint.result!!.classDef

        /*
         * No context parameter method.
         */
        MiniPlayerOverrideNoContextFingerprint.resolve(context, miniPlayerClass)
        val (method, _, parameterRegister) = MiniPlayerOverrideNoContextFingerprint.addProxyCall()

        // Insert right before the return instruction.
        val secondInsertIndex = method.implementation!!.instructions.size - 1
        method.insertOverride(
            secondInsertIndex, parameterRegister
            /** same register used to return **/
        )

        /*
         * Override every return instruction with the proxy call.
         */
        MiniPlayerOverrideFingerprint.result?.let { result ->
            result.mutableMethod.let { method ->
                val appNameStringIndex = result.scanResult.stringsScanResult!!.matches.first().index + 2
                context.toMethodWalker(method).nextMethod(appNameStringIndex, true)
                    .getMethod() as MutableMethod
            }.apply {
                implementation!!.let { implementation ->
                    val returnIndices = implementation.instructions
                        .withIndex()
                        .filter { (_, instruction) -> instruction.opcode == Opcode.RETURN }
                        .map { (index, _) -> index }

                    if (returnIndices.isEmpty()) throw PatchException("No return instructions found.")

                    // This method clobbers register p0 to return the value, calculate to override.
                    val returnedRegister = implementation.registerCount - parameters.size

                    // Hook the returned register on every return instruction.
                    returnIndices.forEach { index -> insertOverride(index, returnedRegister) }
                }
            }

            return@let
        } ?: throw MiniPlayerOverrideFingerprint.exception

        /*
         * Size check return value override.
         */
        MiniPlayerResponseModelSizeCheckFingerprint.addProxyCall()
    }

    // Helper methods.
    private fun MethodFingerprint.addProxyCall(): Triple<MutableMethod, Int, Int> {
        val (method, scanIndex, parameterRegister) = this.unwrap()
        method.insertOverride(scanIndex, parameterRegister)

        return Triple(method, scanIndex, parameterRegister)
    }

    private fun MutableMethod.insertOverride(index: Int, overrideRegister: Int) {
        this.addInstructions(
            index,
            """
                    invoke-static {v$overrideRegister}, Lapp/revanced/integrations/youtube/patches/TabletMiniPlayerOverridePatch;->getTabletMiniPlayerOverride(Z)Z
                    move-result v$overrideRegister
                """
        )
    }

    private fun MethodFingerprint.unwrap(): Triple<MutableMethod, Int, Int> {
        val result = this.result!!
        val scanIndex = result.scanResult.patternScanResult!!.endIndex
        val method = result.mutableMethod
        val instructions = method.implementation!!.instructions
        val parameterRegister = (instructions[scanIndex] as OneRegisterInstruction).registerA

        return Triple(method, scanIndex, parameterRegister)
    }
}
