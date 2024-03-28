package app.revanced.patches.music.misc.settings

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.packagename.ChangePackageNamePatch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.music.misc.integrations.IntegrationsPatch
import app.revanced.patches.music.misc.settings.fingerprints.FullStackTraceActivityFingerprint
import app.revanced.patches.shared.misc.settings.preference.BasePreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.IntentPreference
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.util.MethodUtil
import java.io.Closeable

@Patch(
    description = "Adds settings for ReVanced to YouTube Music.",
    dependencies = [
        IntegrationsPatch::class,
        SettingsResourcePatch::class,
        AddResourcesPatch::class
    ]
)
object SettingsPatch : BytecodePatch(
    setOf(FullStackTraceActivityFingerprint)
), Closeable {
    private const val INTEGRATIONS_PACKAGE = "app/revanced/integrations/music"
    private const val ACTIVITY_HOOK_CLASS_DESCRIPTOR = "L$INTEGRATIONS_PACKAGE/settings/FullStackTraceActivityHook;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        FullStackTraceActivityFingerprint.result?.let { result ->
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
        } ?: throw FullStackTraceActivityFingerprint.exception
    }

    /**
     * Creates an intent to open ReVanced settings.
     */
    fun newIntent(settingsName: String) = IntentPreference.Intent(
        data = settingsName,
        targetClass = "com.google.android.libraries.strictmode.penalties.notification.FullStackTraceActivity"
    ) {
        // The package name change has to be reflected in the intent.
        ChangePackageNamePatch.setOrGetFallbackPackageName("com.google.android.apps.youtube.music")
    }

    object PreferenceScreen : BasePreferenceScreen() {
        val MISC = Screen("revanced_misc_screen")

        override fun commit(screen: app.revanced.patches.shared.misc.settings.preference.PreferenceScreen) {
            SettingsResourcePatch += screen
        }
    }

    override fun close() = PreferenceScreen.close()
}
