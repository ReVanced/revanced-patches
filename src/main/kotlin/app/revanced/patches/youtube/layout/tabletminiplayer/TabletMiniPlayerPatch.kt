package app.revanced.patches.youtube.layout.tabletminiplayer

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.tabletminiplayer.fingerprints.miniPlayerDimensionsCalculatorParentFingerprint
import app.revanced.patches.youtube.layout.tabletminiplayer.fingerprints.miniPlayerOverrideFingerprint
import app.revanced.patches.youtube.layout.tabletminiplayer.fingerprints.miniPlayerOverrideNoContextFingerprint
import app.revanced.patches.youtube.layout.tabletminiplayer.fingerprints.miniPlayerResponseModelSizeCheckFingerprint
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val tabletMiniPlayerPatch = bytecodePatch(
    name = "Tablet mini player",
    description = "Adds an option to enable the tablet mini player layout.",
) {
    dependsOn(
        integrationsPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
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
        ),
    )

    val miniPlayerDimensionsCalculatorParentResult by miniPlayerDimensionsCalculatorParentFingerprint
    val miniPlayerOverrideResult by miniPlayerOverrideFingerprint
    miniPlayerResponseModelSizeCheckFingerprint()

    execute { context ->
        addResources("youtube", "layout.tabletminiplayer.TabletMiniPlayerPatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_tablet_miniplayer"),
        )

        // No context parameter method.
        val (method, _, parameterRegister) = miniPlayerOverrideNoContextFingerprint.apply {
            resolve(context, miniPlayerDimensionsCalculatorParentResult.classDef)
        }.addProxyCall()
        method.insertOverride(method.getInstructions().size - 1, parameterRegister)

        // Override every return instruction with the proxy call.
        context.navigator(miniPlayerOverrideResult.mutableMethod).at(
            miniPlayerOverrideResult.scanResult.stringsScanResult!!.matches.first().index + 2,
        ).mutable().apply {
            val returnIndices = getInstructions().withIndex()
                .filter { (_, instruction) -> instruction.opcode == Opcode.RETURN }
                .map { (index, _) -> index }

            // This method uses register p0 to return the value, calculate to override.
            val returnedRegister = implementation!!.registerCount - parameters.size

            // Hook the returned register on every return instruction.
            returnIndices.forEach { index -> insertOverride(index, returnedRegister) }
        }

        // Size check return value override..
        miniPlayerResponseModelSizeCheckFingerprint.addProxyCall()
    }
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
        """,
    )
}

private fun MethodFingerprint.unwrap(): Triple<MutableMethod, Int, Int> {
    val result = result!!
    val scanIndex = result.scanResult.patternScanResult!!.endIndex
    val method = result.mutableMethod
    val parameterRegister = method.getInstruction<OneRegisterInstruction>(scanIndex).registerA

    return Triple(method, scanIndex, parameterRegister)
}
