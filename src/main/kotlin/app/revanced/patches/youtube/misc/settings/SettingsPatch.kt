package app.revanced.patches.youtube.misc.settings

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.packagename.ChangePackageNamePatch
import app.revanced.patches.shared.misc.settings.preference.impl.InputType
import app.revanced.patches.shared.misc.settings.preference.impl.IntentPreference
import app.revanced.patches.shared.misc.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.impl.TextPreference
import app.revanced.patches.shared.misc.settings.util.AbstractPreferenceScreen
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.fingerprints.LicenseActivityOnCreateFingerprint
import app.revanced.patches.youtube.misc.settings.fingerprints.SetThemeFingerprint
import app.revanced.util.exception
import app.revanced.util.resource.StringResource
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.util.MethodUtil
import java.io.Closeable

@Patch(
    description = "Adds settings for ReVanced to YouTube.",
    dependencies = [IntegrationsPatch::class, SettingsResourcePatch::class]
)
object SettingsPatch : BytecodePatch(
    setOf(LicenseActivityOnCreateFingerprint, SetThemeFingerprint)
), Closeable {
    private const val INTEGRATIONS_PACKAGE = "app/revanced/integrations/youtube"
    private const val ACTIVITY_HOOK_CLASS_DESCRIPTOR = "L$INTEGRATIONS_PACKAGE/settings/LicenseActivityHook;"

    private const val THEME_HELPER_DESCRIPTOR = "L$INTEGRATIONS_PACKAGE/ThemeHelper;"
    private const val SET_THEME_METHOD_NAME: String = "setTheme"

    override fun execute(context: BytecodeContext) {
        SetThemeFingerprint.result?.mutableMethod?.let { setThemeMethod ->
            setThemeMethod.implementation!!.instructions.mapIndexedNotNull { i, instruction ->
                if (instruction.opcode == Opcode.RETURN_OBJECT) i else null
            }.asReversed().forEach { returnIndex ->
                // The following strategy is to replace the return instruction with the setTheme instruction,
                // then add a return instruction after the setTheme instruction.
                // This is done because the return instruction is a target of another instruction.

                setThemeMethod.apply {
                    // This register is returned by the setTheme method.
                    val register = getInstruction<OneRegisterInstruction>(returnIndex).registerA
                    replaceInstruction(
                        returnIndex,
                        "invoke-static { v$register }, " +
                                "$THEME_HELPER_DESCRIPTOR->$SET_THEME_METHOD_NAME(Ljava/lang/Object;)V"
                    )
                    addInstruction(returnIndex + 1, "return-object v$register")
                }
            }
        } ?: throw SetThemeFingerprint.exception

        // Modify the license activity and remove all existing layout code.
        // Must modify an existing activity and cannot add a new activity to the manifest,
        // as that fails for root installations.
        LicenseActivityOnCreateFingerprint.result?.let { result ->
            result.mutableMethod.addInstructions(
                1,
                """
                    invoke-static { p0 }, $ACTIVITY_HOOK_CLASS_DESCRIPTOR->initialize(Landroid/app/Activity;)V
                    return-void
                """
            )

            // Remove other methods as they will break as the onCreate method is modified above.
            result.mutableClass.apply {
                methods.removeIf { it.name != "onCreate" && !MethodUtil.isConstructor(it) }
            }
        } ?: throw LicenseActivityOnCreateFingerprint.exception

        PreferenceScreen.MISC.addPreferences(
            TextPreference(
                key = null,
                title = StringResource("revanced_pref_import_export_title", "Import / Export"),
                summary = StringResource("revanced_pref_import_export_summary", "Import / Export ReVanced settings"),
                inputType = InputType.TEXT_MULTI_LINE,
                tag = "app.revanced.integrations.shared.settings.preference.ImportExportPreference"
            )
        )
    }

    /**
     * Creates an intent to open ReVanced settings.
     */
    fun newIntent(settingsName: String) = IntentPreference.Intent(
        data = settingsName,
        targetClass = "com.google.android.libraries.social.licenses.LicenseActivity"
    ) {
        // The package name change has to be reflected in the intent.
        ChangePackageNamePatch.setOrGetFallbackPackageName("com.google.android.apps.youtube")
    }

    object PreferenceScreen : AbstractPreferenceScreen() {
        val ADS = Screen("ads", "Ads", "Ad related settings")
        val INTERACTIONS = Screen("interactions", "Interaction", "Settings related to interactions")
        val LAYOUT = Screen("layout", "Layout", "Settings related to the layout")
        val VIDEO = Screen("video", "Video", "Settings related to the video player")
        val MISC = Screen("misc", "Misc", "Miscellaneous patches")

        override fun commit(screen: app.revanced.patches.shared.misc.settings.preference.impl.PreferenceScreen) =
            SettingsResourcePatch.addPreference(screen)
    }

    override fun close() = PreferenceScreen.close()
}
