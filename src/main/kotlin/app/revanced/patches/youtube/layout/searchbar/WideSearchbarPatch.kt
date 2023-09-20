package app.revanced.patches.youtube.layout.searchbar

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.searchbar.fingerprints.CreateSearchSuggestionsFingerprint
import app.revanced.patches.youtube.layout.searchbar.fingerprints.SetWordmarkHeaderFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Wide searchbar",
    description = "Replaces the search icon with a wide search bar. This will hide the YouTube logo when active.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.16.37",
                "18.19.35",
                "18.20.39",
                "18.23.35",
                "18.29.38",
                "18.32.39"
            ]
        )
    ]
)
@Suppress("unused")
object WideSearchbarPatch : BytecodePatch(
    setOf(
        SetWordmarkHeaderFingerprint,
        CreateSearchSuggestionsFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_wide_searchbar",
                StringResource("revanced_wide_searchbar_enabled_title", "Enable wide search bar"),
                StringResource("revanced_wide_searchbar_summary_on", "Wide search bar is enabled"),
                StringResource("revanced_wide_searchbar_summary_off", "Wide search bar is disabled")
            )
        )

        val result = CreateSearchSuggestionsFingerprint.result ?: throw CreateSearchSuggestionsFingerprint.exception

        // patch methods
        mapOf(
            SetWordmarkHeaderFingerprint to 1,
            CreateSearchSuggestionsFingerprint to result.scanResult.patternScanResult!!.startIndex
        ).forEach { (fingerprint, callIndex) ->
            context.walkMutable(callIndex, fingerprint).injectSearchBarHook()
        }
    }

    /**
     * Walk a fingerprints method at a given index mutably.
     *
     * @param index The index to walk at.
     * @param fromFingerprint The fingerprint to walk the method on.
     * @return The [MutableMethod] which was walked on.
     */
    fun BytecodeContext.walkMutable(index: Int, fromFingerprint: MethodFingerprint) =
        fromFingerprint.result?.let {
            toMethodWalker(it.method).nextMethod(index, true).getMethod() as MutableMethod
        } ?: throw fromFingerprint.exception


    /**
     * Injects instructions required for certain methods.
     */
    fun MutableMethod.injectSearchBarHook() {
        addInstructions(
            implementation!!.instructions.size - 1,
            """
                    invoke-static {}, Lapp/revanced/integrations/patches/WideSearchbarPatch;->enableWideSearchbar()Z
                    move-result p0
                """
        )
    }
}
