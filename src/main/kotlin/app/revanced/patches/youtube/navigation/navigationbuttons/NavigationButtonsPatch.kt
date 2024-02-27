package app.revanced.patches.youtube.navigation.navigationbuttons

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.navigation.navigationbuttons.fingerprints.AutoMotiveFingerprint
import app.revanced.patches.youtube.navigation.navigationbuttons.fingerprints.PivotBarEnumFingerprint
import app.revanced.patches.youtube.navigation.navigationbuttons.fingerprints.PivotBarShortsButtonViewFingerprint
import app.revanced.patches.youtube.utils.fingerprints.PivotBarCreateButtonViewFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.NAVIGATION
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.ImageOnlyTab
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.getStringInstructionIndex
import app.revanced.util.getTargetIndex
import app.revanced.util.getWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.Opcode.MOVE_RESULT_OBJECT
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Hide navigation buttons",
    description = "Adds options to hide and change navigation buttons (such as the Shorts button).",
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
object NavigationButtonsPatch : BytecodePatch(
    setOf(
        AutoMotiveFingerprint,
        PivotBarCreateButtonViewFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        PivotBarCreateButtonViewFingerprint.result?.let { parentResult ->

            /**
             * Home, Shorts, Subscriptions Button
             */
            with(
                arrayOf(
                    PivotBarEnumFingerprint,
                    PivotBarShortsButtonViewFingerprint
                ).onEach {
                    it.resolve(
                        context,
                        parentResult.mutableMethod,
                        parentResult.mutableClass
                    )
                }.map {
                    it.result?.scanResult?.patternScanResult ?: throw it.exception
                }
            ) {
                val enumScanResult = this[0]
                val buttonViewResult = this[1]

                val enumHookInsertIndex = enumScanResult.startIndex + 2
                val buttonHookInsertIndex = buttonViewResult.endIndex

                mapOf(
                    BUTTON_HOOK to buttonHookInsertIndex,
                    ENUM_HOOK to enumHookInsertIndex
                ).forEach { (hook, insertIndex) ->
                    parentResult.mutableMethod.injectHook(hook, insertIndex)
                }
            }

            /**
             * Create Button
             */
            parentResult.mutableMethod.apply {
                val constIndex = getWideLiteralInstructionIndex(ImageOnlyTab)
                val insertIndex = getTargetIndex(constIndex, Opcode.INVOKE_VIRTUAL) + 2
                injectHook(CREATE_BUTTON_HOOK, insertIndex)
            }

        } ?: throw PivotBarCreateButtonViewFingerprint.exception

        /**
         * Switch create button with notifications button
         */
        AutoMotiveFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = getStringInstructionIndex("Android Automotive") - 1
                val register = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex, """
                        invoke-static {v$register}, $NAVIGATION->switchCreateNotification(Z)Z
                        move-result v$register
                        """
                )
            }
        } ?: throw AutoMotiveFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: NAVIGATION_SETTINGS",
                "SETTINGS: HIDE_NAVIGATION_BUTTONS"
            )
        )

        SettingsPatch.updatePatchStatus("Hide navigation buttons")

    }

    private const val REGISTER_TEMPLATE_REPLACEMENT: String = "REGISTER_INDEX"

    private const val ENUM_HOOK =
        "sput-object v$REGISTER_TEMPLATE_REPLACEMENT, $NAVIGATION" +
                "->" +
                "lastPivotTab:Ljava/lang/Enum;"

    private const val BUTTON_HOOK =
        "invoke-static { v$REGISTER_TEMPLATE_REPLACEMENT }, $NAVIGATION" +
                "->" +
                "hideNavigationButton(Landroid/view/View;)V"

    private const val CREATE_BUTTON_HOOK =
        "invoke-static { v$REGISTER_TEMPLATE_REPLACEMENT }, $NAVIGATION" +
                "->" +
                "hideCreateButton(Landroid/view/View;)V"

    /**
     * Injects an instruction into insertIndex of the hook.
     * @param hook The hook to insert.
     * @param insertIndex The index to insert the instruction at.
     * [MOVE_RESULT_OBJECT] has to be the previous instruction before [insertIndex].
     */
    private fun MutableMethod.injectHook(hook: String, insertIndex: Int) {
        val injectTarget = this

        // Register to pass to the hook
        val registerIndex = insertIndex - 1 // MOVE_RESULT_OBJECT is always the previous instruction
        val register = injectTarget.getInstruction<OneRegisterInstruction>(registerIndex).registerA

        injectTarget.addInstruction(
            insertIndex,
            hook.replace("REGISTER_INDEX", register.toString()),
        )
    }
}