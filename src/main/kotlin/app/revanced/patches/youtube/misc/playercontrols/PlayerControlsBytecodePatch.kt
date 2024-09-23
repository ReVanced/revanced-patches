package app.revanced.patches.youtube.misc.playercontrols

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.youtube.misc.playercontrols.fingerprints.ControlsOverlayVisibility
import app.revanced.patches.youtube.misc.playercontrols.fingerprints.OverlayViewInflateFingerprint
import app.revanced.patches.youtube.misc.playercontrols.fingerprints.PlayerBottomControlsInflateFingerprint
import app.revanced.patches.youtube.misc.playercontrols.fingerprints.PlayerControlsIntegrationHookFingerprint
import app.revanced.patches.youtube.misc.playercontrols.fingerprints.PlayerTopControlsInflateFingerprint
import app.revanced.util.alsoResolve
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstWideLiteralInstructionValueReversedOrThrow
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

@Patch(
    description = "Manages the code for the player controls of the YouTube player.",
    dependencies = [PlayerControlsResourcePatch::class],
)
object PlayerControlsBytecodePatch : BytecodePatch(
    setOf(
        PlayerTopControlsInflateFingerprint,
        PlayerBottomControlsInflateFingerprint,
        OverlayViewInflateFingerprint,
        PlayerControlsIntegrationHookFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/PlayerControlsPatch;"

    private lateinit var inflateTopControlMethod: MutableMethod
    private var inflateTopControlInsertIndex: Int = -1
    private var inflateTopControlRegister: Int = -1

    private lateinit var inflateBottomControlMethod: MutableMethod
    private var inflateBottomControlInsertIndex: Int = -1
    private var inflateBottomControlRegister: Int = -1

    private lateinit var visibilityMethod: MutableMethod
    private var visibilityInsertIndex: Int = 0

    private lateinit var visibilityImmediateMethod: MutableMethod
    private var visibilityImmediateInsertIndex: Int = 0

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

        // Hook the fullscreen close button.  Used to fix visibility
        // when seeking and other situations.
        OverlayViewInflateFingerprint.resultOrThrow().mutableMethod.apply {
            val resourceIndex = indexOfFirstWideLiteralInstructionValueReversedOrThrow(
                PlayerControlsResourcePatch.fullscreenButton
            )

            val index = indexOfFirstInstructionOrThrow(resourceIndex) {
                opcode == Opcode.CHECK_CAST && getReference<TypeReference>()?.type ==
                        "Landroid/widget/ImageView;"
            }
            val register = getInstruction<OneRegisterInstruction>(index).registerA

            addInstruction(index + 1, "invoke-static { v$register }, " +
                    "$INTEGRATIONS_CLASS_DESCRIPTOR->setFullscreenCloseButton(Landroid/widget/ImageView;)V")
        }
        
        visibilityImmediateMethod = PlayerControlsIntegrationHookFingerprint.resultOrThrow().mutableMethod
    }

    /**
     * Injects the code to initialize the controls.
     * @param descriptor The descriptor of the method which should be called.
     */
    internal fun initializeTopControl(descriptor: String) {
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
    /**
     * Injects the code to change the visibility of controls.
     * @param descriptor The descriptor of the method which should be called.
     */
    fun injectVisibilityCheckCall(descriptor: String) {
        visibilityMethod.addInstruction(
            visibilityInsertIndex++,
            "invoke-static { p1 , p2 }, $descriptor->changeVisibility(ZZ)V"
        )

        visibilityImmediateMethod.addInstruction(
            visibilityImmediateInsertIndex++,
            "invoke-static { p0 }, $descriptor->changeVisibilityImmediate(Z)V"
        )
    }


    @Deprecated("Obsolete", replaceWith = ReplaceWith("initializeBottomControl"))
    fun initializeControl(descriptor: String)= initializeBottomControl(descriptor)
}