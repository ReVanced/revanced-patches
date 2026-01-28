package app.revanced.patches.tiktok.misc.settings

import app.revanced.patcher.extensions.*
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.layout.branding.addBrandLicensePatch
import app.revanced.patches.tiktok.misc.extension.sharedExtensionPatch
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/tiktok/settings/TikTokActivityHook;"

val Settings = bytecodePatch(
    description = "Adds ReVanced settings to TikTok.",
) {
    dependsOn(sharedExtensionPatch, addBrandLicensePatch)

    compatibleWith(
        "com.ss.android.ugc.trill"("36.5.4"),
        "com.zhiliaoapp.musically"("36.5.4"),
    )

    apply {
        val initializeSettingsMethodDescriptor =
            "$EXTENSION_CLASS_DESCRIPTOR->initialize(" +
                "Lcom/bytedance/ies/ugc/aweme/commercialize/compliance/personalization/AdPersonalizationActivity;" +
                ")Z"

        val createSettingsEntryMethodDescriptor =
            "$EXTENSION_CLASS_DESCRIPTOR->createSettingsEntry(" +
                "Ljava/lang/String;" +
                "Ljava/lang/String;" +
                ")Ljava/lang/Object;"

        fun String.toClassName() = substring(1, this.length - 1).replace("/", ".")

        // Find the class name of classes which construct a settings entry
        val settingsButtonClass = settingsEntryMethod.immutableClassDef.type.toClassName()
        val settingsButtonInfoClass = settingsEntryInfoMethod.immutableClassDef.type.toClassName()

        // Create a settings entry for 'revanced settings' and add it to settings fragment
        addSettingsEntryMethod.apply {
            val markIndex = indexOfFirstInstruction {
                opcode == Opcode.IGET_OBJECT && fieldReference?.name == "headerUnit"
            }

            val getUnitManager = getInstruction(markIndex + 2)
            val addEntry = getInstruction(markIndex + 1)

            addInstructions(
                markIndex + 2,
                listOf(
                    getUnitManager,
                    addEntry,
                ),
            )

            addInstructions(
                markIndex + 2,
                """
                    const-string v0, "$settingsButtonClass"
                    const-string v1, "$settingsButtonInfoClass"
                    invoke-static {v0, v1}, $createSettingsEntryMethodDescriptor
                    move-result-object v0
                    check-cast v0, ${settingsEntryMethod.immutableClassDef.type}
                """,
            )
        }

        // Initialize the settings menu once the replaced setting entry is clicked.
        adPersonalizationActivityOnCreateMethod.apply {
            val initializeSettingsIndex = indexOfFirstInstruction(Opcode.INVOKE_SUPER) + 1

            val thisRegister = getInstruction<FiveRegisterInstruction>(initializeSettingsIndex - 1).registerC
            val usableRegister = implementation!!.registerCount - parameters.size - 2

            addInstructionsWithLabels(
                initializeSettingsIndex,
                """
                    invoke-static { v$thisRegister }, $initializeSettingsMethodDescriptor
                    move-result v$usableRegister
                    if-eqz v$usableRegister, :do_not_open
                    return-void
                """,
                ExternalLabel("do_not_open", getInstruction(initializeSettingsIndex)),
            )
        }
    }
}
