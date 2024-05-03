package app.revanced.patches.tiktok.misc.settings

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.tiktok.misc.settings.fingerprints.adPersonalizationActivityOnCreateFingerprint
import app.revanced.patches.tiktok.misc.settings.fingerprints.addSettingsEntryFingerprint
import app.revanced.patches.tiktok.misc.settings.fingerprints.settingsEntryFingerprint
import app.revanced.patches.tiktok.misc.settings.fingerprints.settingsEntryInfoFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22c
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Suppress("unused")
val settingsPatch = bytecodePatch(
    name = "Settings",
    description = "Adds ReVanced settings to TikTok.",
) {
    compatibleWith(
        "com.ss.android.ugc.trill"("32.5.3"),
        "com.zhiliaoapp.musically"("32.5.3")
    )

    val adPersonalizationActivityOnCreateResult by adPersonalizationActivityOnCreateFingerprint
    val addSettingsEntryResult by addSettingsEntryFingerprint
    val settingsEntryResult by settingsEntryFingerprint
    val settingsEntryInfoResult by settingsEntryInfoFingerprint

    val integrationsClassDescriptor =
        "Lapp/revanced/integrations/tiktok/settings/AdPersonalizationActivityHook;"

    val initializeSettingsMethodDescriptor =
        "$integrationsClassDescriptor->initialize(" +
                "Lcom/bytedance/ies/ugc/aweme/commercialize/compliance/personalization/AdPersonalizationActivity;" +
                ")Z"

    val createSettingsEntryMethodDescriptor =
        "$integrationsClassDescriptor->createSettingsEntry(" +
                "Ljava/lang/String;" +
                "Ljava/lang/String;" +
                ")Ljava/lang/Object;"

    fun String.toClassName(): String {
        return substring(1, this.length - 1).replace("/", ".")
    }

    execute {
        // Find the class name of classes which construct a settings entry
        val settingsButtonClass = settingsEntryResult.classDef.type.toClassName()
        val settingsButtonInfoClass = settingsEntryInfoResult.classDef.type.toClassName()

        // Create a settings entry for 'revanced settings' and add it to settings fragment
        addSettingsEntryResult.mutableMethod.apply {
            val markIndex = implementation!!.instructions.indexOfFirst {
                it.opcode == Opcode.IGET_OBJECT && ((it as Instruction22c).reference as FieldReference).name == "headerUnit"
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
                """,
            )
        }

        // Initialize the settings menu once the replaced setting entry is clicked.
        adPersonalizationActivityOnCreateResult.mutableMethod.apply {
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
    }
}
