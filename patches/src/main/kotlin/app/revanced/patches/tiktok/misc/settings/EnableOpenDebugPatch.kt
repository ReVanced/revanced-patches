package app.revanced.patches.tiktok.misc.settings

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.tiktok.misc.extension.sharedExtensionPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22c
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val SETTINGS_EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/tiktok/settings/TikTokActivityHook;"

@Suppress("unused")
val enableOpenDebugPatch = bytecodePatch(
    name = "Enable Open Debug",
    description = "Re-enables the hidden \"Open debug\" entry in TikTok settings.",
) {
    dependsOn(sharedExtensionPatch)

    compatibleWith(
        "com.ss.android.ugc.trill"("43.6.2"),
        "com.zhiliaoapp.musically"("43.6.2"),
    )

    execute {
        val initializeSettingsMethodDescriptor =
            "$SETTINGS_EXTENSION_CLASS_DESCRIPTOR->initialize(" +
                "Lcom/bytedance/ies/ugc/aweme/commercialize/compliance/personalization/AdPersonalizationActivity;" +
                ")Z"

        // Show the entry in the "Support" group.
        supportGroupDefaultStateFingerprint.method.apply {
            val aboutSgetIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.SGET_OBJECT && getReference<FieldReference>()?.name == "ABOUT"
            }

            val aboutAddInstruction = getInstruction<Instruction35c>(aboutSgetIndex + 1)
            val listRegister = aboutAddInstruction.registerC
            val itemRegister = aboutAddInstruction.registerD

            addInstructions(
                aboutSgetIndex + 2,
                """
                    sget-object v$itemRegister, LX/0mDW;->OPEN_DEBUG:LX/0mDW;
                    invoke-virtual { v$listRegister, v$itemRegister }, LX/165P;->add(Ljava/lang/Object;)Z
                """,
            )
        }

        // Initialize the ReVanced settings UI when AdPersonalizationActivity is opened with our marker extra.
        adPersonalizationActivityOnCreateFingerprint.method.apply {
            val initializeSettingsIndex = implementation!!.instructions.indexOfFirst {
                it.opcode == Opcode.INVOKE_SUPER
            } + 1

            val thisRegister = getInstruction<Instruction35c>(initializeSettingsIndex - 1).registerC
            val usableRegister = implementation!!.registerCount - parameters.size - 2

            addInstructionsWithLabels(
                initializeSettingsIndex,
                """
                    invoke-static {v$thisRegister}, $initializeSettingsMethodDescriptor
                    move-result v$usableRegister
                    if-eqz v$usableRegister, :do_not_open
                    return-void
                """,
                ExternalLabel("do_not_open", getInstruction(initializeSettingsIndex)),
            )
        }

        // Set a custom label ("ReVanced settings") for the entry.
        openDebugCellStateConstructorFingerprint.method.apply {
            val titleValuePutIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.IPUT_OBJECT && getReference<FieldReference>()?.name == "LLILLL"
            }

            val valueRegister = getInstruction<Instruction22c>(titleValuePutIndex).registerA
            addInstruction(titleValuePutIndex, "const-string v$valueRegister, \"ReVanced settings\"")
        }

        // Prefer the "titleValue" field over resolving the "titleId" resource.
        openDebugCellComposeFingerprint.method.apply {
            val getStringInvokeIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_VIRTUAL &&
                    getReference<MethodReference>()?.toString() ==
                    "Landroid/content/Context;->getString(I)Ljava/lang/String;"
            }

            val moveResultIndex = getStringInvokeIndex + 1
            val afterTitleIndex = getStringInvokeIndex + 2

            val titleStringRegister = getInstruction<OneRegisterInstruction>(moveResultIndex).registerA

            val titleIdFieldGetIndex = indexOfFirstInstructionReversedOrThrow(getStringInvokeIndex) {
                opcode == Opcode.IGET_OBJECT && getReference<FieldReference>()?.name == "LLILL"
            }
            val stateRegister = getInstruction<Instruction22c>(titleIdFieldGetIndex).registerB

            addInstructionsWithLabels(
                getStringInvokeIndex,
                """
                    iget-object v$titleStringRegister, v$stateRegister, LX/05iN;->LLILLL:Ljava/lang/String;
                    if-nez v$titleStringRegister, :revanced_title_done
                """,
                ExternalLabel("revanced_title_done", getInstruction(afterTitleIndex)),
            )
        }

        // Swap the icon to a built-in gear icon.
        openDebugCellVmDefaultStateFingerprint.method.apply {
            val iconIdLiteralIndex = indexOfFirstInstructionOrThrow {
                this is NarrowLiteralInstruction && narrowLiteral == 0x7f0107e3
            }

            val iconRegister = getInstruction<OneRegisterInstruction>(iconIdLiteralIndex).registerA

            // raw/icon_2pt_settings_stroke
            replaceInstruction(iconIdLiteralIndex, "const v$iconRegister, 0x7f010088")
        }

        // Wire up the click action to open ReVanced settings.
        openDebugCellClickWrapperFingerprint.method.apply {
            addInstructions(
                0,
                """
                    iget-object v0, p0, Lkotlin/jvm/internal/AwS350S0200000_2;->l1:Ljava/lang/Object;
                    check-cast v0, Landroid/content/Context;
                    new-instance v1, Landroid/content/Intent;
                    const-class v2, Lcom/bytedance/ies/ugc/aweme/commercialize/compliance/personalization/AdPersonalizationActivity;
                    invoke-direct { v1, v0, v2 }, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V
                    const-string v2, "revanced_settings"
                    invoke-virtual { v1, v2 }, Landroid/content/Intent;->setAction(Ljava/lang/String;)Landroid/content/Intent;
                    const/high16 v2, 0x10000000
                    invoke-virtual { v1, v2 }, Landroid/content/Intent;->addFlags(I)Landroid/content/Intent;
                    invoke-virtual { v0, v1 }, Landroid/content/Context;->startActivity(Landroid/content/Intent;)V
                    sget-object v0, Lkotlin/Unit;->LIZ:Lkotlin/Unit;
                    return-object v0
                """,
            )
        }
    }
}
