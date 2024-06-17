package app.revanced.patches.youtube.misc.playercontrols

import app.revanced.patcher.Match
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.shared.layoutConstructorFingerprint
import app.revanced.util.matchOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

/**
 * Injects the code to change the visibility of controls.
 * @param descriptor The descriptor of the method which should be called.
 */
fun injectVisibilityCheckCall(descriptor: String) = showPlayerControlsMatch.mutableMethod.addInstruction(
    0,
    "invoke-static { p1 }, $descriptor",
)

/**
 * Injects the code to initialize the controls.
 * @param descriptor The descriptor of the method which should be called.
 */
fun initializeControl(descriptor: String) = inflateMatch.mutableMethod.addInstruction(
    moveToRegisterInstructionIndex + 1,
    "invoke-static {v$viewRegister}, $descriptor",
)

lateinit var showPlayerControlsMatch: Match
    private set

private var moveToRegisterInstructionIndex = 0
private var viewRegister = 0
private lateinit var inflateMatch: Match

val playerControlsPatch = bytecodePatch(
    description = "Manages the code for the player controls of the YouTube player.",
) {
    dependsOn(bottomControlsPatch)

    val layoutConstructorMatch by layoutConstructorFingerprint()
    val bottomControlsInflateMatch by bottomControlsInflateFingerprint()

    execute { context ->
        showPlayerControlsMatch = playerControlsVisibilityFingerprint.apply {
            match(context, layoutConstructorMatch.classDef)
        }.matchOrThrow()

        inflateMatch = bottomControlsInflateMatch.also {
            moveToRegisterInstructionIndex = it.patternMatch!!.endIndex
            viewRegister = it.method.getInstruction<OneRegisterInstruction>(moveToRegisterInstructionIndex).registerA
        }
    }
}
