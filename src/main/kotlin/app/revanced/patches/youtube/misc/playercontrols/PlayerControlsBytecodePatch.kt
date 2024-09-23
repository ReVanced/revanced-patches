package app.revanced.patches.youtube.misc.playercontrols

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.misc.playercontrols.fingerprints.PlayerBottomControlsInflateFingerprint
import app.revanced.patches.youtube.misc.playercontrols.fingerprints.PlayerTopControlsInflateFingerprint
import app.revanced.patches.youtube.misc.playercontrols.fingerprints.MotionOverlayVisibilityFingerprint
import app.revanced.patches.youtube.misc.playercontrols.fingerprints.MotionOverlayFingerprint
import app.revanced.patches.youtube.misc.playercontrols.fingerprints.ControlsOverlayVisibility
import app.revanced.util.alsoResolve
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    description = "Manages the code for the player controls of the YouTube player.",
    dependencies = [PlayerControlsResourcePatch::class],
)
object PlayerControlsBytecodePatch : BytecodePatch(
    setOf(
        PlayerTopControlsInflateFingerprint,
        PlayerBottomControlsInflateFingerprint,
        MotionOverlayFingerprint
    )
) {
    private lateinit var inflateTopControlMethod: MutableMethod
    private var inflateTopControlInsertIndex: Int = -1
    private var inflateTopControlRegister: Int = -1

    private lateinit var inflateBottomControlMethod: MutableMethod
    private var inflateBottomControlInsertIndex: Int = -1
    private var inflateBottomControlRegister: Int = -1

    private lateinit var visibilityMethod: MutableMethod
    private var visibilityInsertIndex: Int = 0

    private lateinit var invertVisibilityMethod: MutableMethod
    private var invertVisibilityInsertIndex: Int = 0

    override fun execute(context: BytecodeContext) {
        fun MutableMethod.indexOfFirstViewInflateOrThrow() =
            indexOfFirstInstructionOrThrow {
                val reference = getReference<MethodReference>()
                reference?.definingClass == "Landroid/view/ViewStub;" &&
                        reference.name == "inflate"
            }

        PlayerBottomControlsInflateFingerprint.resultOrThrow().mutableMethod.apply{
            inflateBottomControlMethod = this

            val inflateReturnObjectIndex = indexOfFirstViewInflateOrThrow() + 1
            inflateBottomControlRegister = getInstruction<OneRegisterInstruction>(inflateReturnObjectIndex).registerA
            inflateBottomControlInsertIndex = inflateReturnObjectIndex + 1
        }

        PlayerTopControlsInflateFingerprint.resultOrThrow().mutableMethod.apply {
            inflateTopControlMethod = this

            val inflateReturnObjectIndex = indexOfFirstViewInflateOrThrow() + 1
            inflateTopControlRegister = getInstruction<OneRegisterInstruction>(inflateReturnObjectIndex).registerA
            inflateTopControlInsertIndex = inflateReturnObjectIndex + 1
        }

        ControlsOverlayVisibility.alsoResolve(
            context, PlayerTopControlsInflateFingerprint
        ).mutableMethod.apply {
            visibilityMethod = this
        }

        MotionOverlayVisibilityFingerprint.alsoResolve(
            context, MotionOverlayFingerprint
        ).mutableMethod.apply {
            invertVisibilityMethod = this
        }
    }

    /**
     * Injects the code to initialize the controls.
     * @param descriptor The descriptor of the method which should be called.
     */
    fun initializeTopControl(descriptor: String) {
        inflateTopControlMethod.addInstruction(
            inflateTopControlInsertIndex++,
            "invoke-static { v$inflateTopControlRegister }, $descriptor->initialize(Landroid/view/View;)V"
        )
    }

    /**
     * Injects the code to initialize the controls.
     * @param descriptor The descriptor of the method which should be called.
     */
    fun initializeBottomControl(descriptor: String) {
        inflateBottomControlMethod.addInstruction(
            inflateBottomControlInsertIndex++,
            "invoke-static { v$inflateBottomControlRegister }, $descriptor->initializeButton(Landroid/view/View;)V"
        )
    }

    @Deprecated("Obsolete", replaceWith = ReplaceWith("initializeBottomControl"))
    fun initializeControl(descriptor: String)= initializeBottomControl(descriptor)

    /**
     * Injects the code to change the visibility of controls.
     * @param descriptor The descriptor of the method which should be called.
     */
    fun injectVisibilityCheckCall(descriptor: String) {
        visibilityMethod.addInstruction(
            visibilityInsertIndex++,
            "invoke-static { p1 }, $descriptor->changeVisibility(Z)V"
        )

        // Edit: It's not clear if this hook is still needed,
        // and it only seems to be called on app startup.
        invertVisibilityMethod.addInstruction(
            invertVisibilityInsertIndex++,
            "invoke-static { p1 }, $descriptor->changeVisibilityNegatedImmediate(Z)V"
        )
    }
}