package app.revanced.patches.youtube.general.accountmenu

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.general.accountmenu.fingerprints.AccountListFingerprint
import app.revanced.patches.youtube.general.accountmenu.fingerprints.AccountListParentFingerprint
import app.revanced.patches.youtube.general.accountmenu.fingerprints.AccountMenuFingerprint
import app.revanced.patches.youtube.general.accountmenu.fingerprints.AccountMenuParentFingerprint
import app.revanced.patches.youtube.general.accountmenu.fingerprints.AccountMenuPatchFingerprint
import app.revanced.patches.youtube.general.accountmenu.fingerprints.SetViewGroupMarginFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.GENERAL
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Patch(
    name = "Hide account menu",
    description = "Adds the ability to hide account menu elements using a custom filter in the account menu and You tab.",
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
object AccountMenuPatch : BytecodePatch(
    setOf(
        AccountListParentFingerprint,
        AccountMenuParentFingerprint,
        AccountMenuPatchFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        AccountListParentFingerprint.result?.let { parentResult ->
            AccountListFingerprint.also {
                it.resolve(
                    context,
                    parentResult.classDef
                )
            }.result?.let {
                it.mutableMethod.apply {
                    val targetIndex = it.scanResult.patternScanResult!!.startIndex + 3
                    val targetInstruction = getInstruction<FiveRegisterInstruction>(targetIndex)

                    addInstruction(
                        targetIndex,
                        "invoke-static {v${targetInstruction.registerC}, v${targetInstruction.registerD}}, " +
                                "$GENERAL->hideAccountList(Landroid/view/View;Ljava/lang/CharSequence;)V"
                    )
                }
            } ?: throw AccountListFingerprint.exception
        } ?: throw AccountListParentFingerprint.exception

        AccountMenuParentFingerprint.result?.let { parentResult ->
            AccountMenuFingerprint.also {
                it.resolve(
                    context,
                    parentResult.classDef
                )
            }.result?.let {
                it.mutableMethod.apply {
                    val targetIndex = it.scanResult.patternScanResult!!.startIndex + 2
                    val targetInstruction = getInstruction<FiveRegisterInstruction>(targetIndex)

                    addInstruction(
                        targetIndex,
                        "invoke-static {v${targetInstruction.registerC}, v${targetInstruction.registerD}}, " +
                                "$GENERAL->hideAccountMenu(Landroid/view/View;Ljava/lang/CharSequence;)V"
                    )
                }
            } ?: throw AccountMenuFingerprint.exception

            SetViewGroupMarginFingerprint.also {
                it.resolve(
                    context,
                    parentResult.classDef
                )
            }.result?.let {
                it.mutableMethod.apply {
                    val setViewGroupMarginIndex = it.scanResult.patternScanResult!!.startIndex
                    val setViewGroupMarginReference =
                        getInstruction<ReferenceInstruction>(setViewGroupMarginIndex).reference

                    AccountMenuPatchFingerprint.result?.mutableMethod?.addInstructions(
                        0, """
                            const/4 v0, 0x0
                            invoke-static {p0, v0, v0}, $setViewGroupMarginReference
                            """
                    ) ?: throw AccountMenuPatchFingerprint.exception
                }
            } ?: throw SetViewGroupMarginFingerprint.exception
        } ?: throw AccountMenuParentFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: GENERAL_SETTINGS",
                "SETTINGS: HIDE_ACCOUNT_MENU"
            )
        )

        SettingsPatch.updatePatchStatus("Hide account menu")

    }
}