package app.revanced.patches.tiktok.misc.settings

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.tiktok.misc.integrations.IntegrationsPatch
import app.revanced.patches.tiktok.misc.settings.fingerprints.AdPersonalizationActivityOnCreateFingerprint
import app.revanced.patches.tiktok.misc.settings.fingerprints.AddSettingsEntryFingerprint
import app.revanced.patches.tiktok.misc.settings.fingerprints.SettingsEntryFingerprint
import app.revanced.patches.tiktok.misc.settings.fingerprints.SettingsEntryInfoFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22c
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Patch(
    name = "Settings",
    description = "Adds ReVanced settings to TikTok.",
    dependencies = [IntegrationsPatch::class],
    compatiblePackages = [
        CompatiblePackage("com.ss.android.ugc.trill", ["32.5.3"]),
        CompatiblePackage("com.zhiliaoapp.musically", ["32.5.3"])
    ]
)
object SettingsPatch : BytecodePatch(
    setOf(
        AdPersonalizationActivityOnCreateFingerprint,
        AddSettingsEntryFingerprint,
        SettingsEntryFingerprint,
        SettingsEntryInfoFingerprint,
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/tiktok/settings/AdPersonalizationActivityHook;"

    private const val INITIALIZE_SETTINGS_METHOD_DESCRIPTOR =
        "$INTEGRATIONS_CLASS_DESCRIPTOR->initialize(" +
                "Lcom/bytedance/ies/ugc/aweme/commercialize/compliance/personalization/AdPersonalizationActivity;" +
                ")Z"

    private const val CREATE_SETTINGS_ENTRY_METHOD_DESCRIPTOR =
        "$INTEGRATIONS_CLASS_DESCRIPTOR->createSettingsEntry(" +
                "Ljava/lang/String;" +
                "Ljava/lang/String;" +
                ")Ljava/lang/Object;"

    override fun execute(context: BytecodeContext) {
        // Find the class name of classes which construct a settings entry
        val settingsButtonClass = SettingsEntryFingerprint.result?.classDef?.type?.toClassName()
            ?: throw SettingsEntryFingerprint.exception
        val settingsButtonInfoClass = SettingsEntryInfoFingerprint.result?.classDef?.type?.toClassName()
            ?: throw SettingsEntryInfoFingerprint.exception

        // Create a settings entry for 'revanced settings' and add it to settings fragment
        AddSettingsEntryFingerprint.result?.mutableMethod?.apply {
            val markIndex = implementation!!.instructions.indexOfFirst {
                it.opcode == Opcode.IGET_OBJECT && ((it as Instruction22c).reference as FieldReference).name == "headerUnit"
            }

            val getUnitManager = getInstruction(markIndex + 2)
            val addEntry = getInstruction(markIndex + 1)

            addInstructions(
                markIndex + 2,
                listOf(
                    getUnitManager,
                    addEntry
                )
            )

            addInstructions(
                markIndex + 2,
                """
                    const-string v0, "$settingsButtonClass"
                    const-string v1, "$settingsButtonInfoClass"
                    invoke-static {v0, v1}, $CREATE_SETTINGS_ENTRY_METHOD_DESCRIPTOR
                    move-result-object v0
                """
            )
        } ?: throw AddSettingsEntryFingerprint.exception

        // Initialize the settings menu once the replaced setting entry is clicked.
        AdPersonalizationActivityOnCreateFingerprint.result?.mutableMethod?.apply {
            val initializeSettingsIndex = implementation!!.instructions.indexOfFirst {
                it.opcode == Opcode.INVOKE_SUPER
            } + 1

            val thisRegister = getInstruction<Instruction35c>(initializeSettingsIndex - 1).registerC
            val usableRegister = implementation!!.registerCount - parameters.size - 2

            addInstructionsWithLabels(
                initializeSettingsIndex,
                """
                    invoke-static {v$thisRegister}, $INITIALIZE_SETTINGS_METHOD_DESCRIPTOR
                    move-result v$usableRegister
                    if-eqz v$usableRegister, :do_not_open
                    return-void
                """,
                ExternalLabel("do_not_open", getInstruction(initializeSettingsIndex))
            )
        } ?: throw AdPersonalizationActivityOnCreateFingerprint.exception
    }

    private fun String.toClassName(): String {
        return substring(1, this.length - 1).replace("/", ".")
    }
}