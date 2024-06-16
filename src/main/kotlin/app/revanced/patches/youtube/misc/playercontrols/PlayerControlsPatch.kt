package app.revanced.patches.youtube.misc.playercontrols

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.shared.layoutConstructorFingerprint
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

/**
 * Injects the code to change the visibility of controls.
 * @param descriptor The descriptor of the method which should be called.
 */
fun injectVisibilityCheckCall(descriptor: String) {
    showPlayerControlsFingerprintResult.mutableMethod.addInstruction(
        0,
        """
            invoke-static {p1}, $descriptor
        """,
    )
}

/**
 * Injects the code to initialize the controls.
 * @param descriptor The descriptor of the method which should be calleed.
 */
fun initializeControl(descriptor: String) {
    inflateFingerprintResult.mutableMethod.addInstruction(
        moveToRegisterInstructionIndex + 1,
        "invoke-static {v$viewRegister}, $descriptor",
    )
}

lateinit var showPlayerControlsFingerprintResult: MethodFingerprintResult
    private set

private var moveToRegisterInstructionIndex = 0
private var viewRegister = 0
private lateinit var inflateFingerprintResult: MethodFingerprintResult

val playerControlsPatch = bytecodePatch(
    description = "Manages the code for the player controls of the YouTube player.",
) {
    dependsOn(bottomControlsPatch)

    val layoutConstructorFingerprintResult by layoutConstructorFingerprint()
    val bottomControlsInflateFingerprintResult by bottomControlsInflateFingerprint()

    execute { context ->
        showPlayerControlsFingerprintResult = playerControlsVisibilityFingerprint.apply {
            resolve(context, layoutConstructorFingerprintResult.classDef)
        }.resultOrThrow()

        inflateFingerprintResult = bottomControlsInflateFingerprintResult.also {
            moveToRegisterInstructionIndex = it.scanResult.patternScanResult!!.endIndex
            viewRegister = it.mutableMethod.getInstruction<OneRegisterInstruction>(moveToRegisterInstructionIndex).registerA
        }
    }
}
