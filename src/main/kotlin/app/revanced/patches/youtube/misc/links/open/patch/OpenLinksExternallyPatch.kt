package app.revanced.patches.youtube.misc.links.open.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.links.open.annotations.OpenLinksExternallyCompatibility
import app.revanced.patches.youtube.misc.links.open.fingerprints.BindSessionServiceFingerprint
import app.revanced.patches.youtube.misc.links.open.fingerprints.GetCustomTabPackageNameFingerprint
import app.revanced.patches.youtube.misc.links.open.fingerprints.InitializeCustomTabSupportFingerprint
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import org.jf.dexlib2.iface.instruction.formats.Instruction21c

@Patch
@Name("open-links-externally")
@Description("Open links outside of the app directly in your browser.")
@OpenLinksExternallyCompatibility
@Version("0.0.1")
class OpenLinksExternallyPatch : BytecodePatch(
    listOf(
        GetCustomTabPackageNameFingerprint,
        BindSessionServiceFingerprint,
        InitializeCustomTabSupportFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference(
                "revanced_enable_external_browser",
                StringResource("revanced_enable_external_browser_title", "Open links in browser"),
                true,
                StringResource("revanced_enable_external_browser_summary_on", "Opening links externally"),
                StringResource("revanced_enable_external_browser_summary_off", "Opening links in app")
            )
        )

        arrayOf(
            GetCustomTabPackageNameFingerprint,
            BindSessionServiceFingerprint,
            InitializeCustomTabSupportFingerprint
        ).forEach {
            val result = it.result ?: return it.toErrorResult()
            val insertIndex = result.scanResult.patternScanResult!!.endIndex + 1
            with(result.mutableMethod) {
                val register = (implementation!!.instructions[insertIndex - 1] as Instruction21c).registerA
                addInstructions(
                    insertIndex, """
                        invoke-static {v$register}, Lapp/revanced/integrations/patches/OpenLinksExternallyPatch;->enableExternalBrowser(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$register
                    """
                )
            }
        }

        return PatchResultSuccess()
    }
}