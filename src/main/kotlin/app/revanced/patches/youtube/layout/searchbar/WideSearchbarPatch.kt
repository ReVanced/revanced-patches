package app.revanced.patches.youtube.layout.searchbar

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.searchbar.fingerprints.CreateSearchSuggestionsFingerprint
import app.revanced.patches.youtube.layout.searchbar.fingerprints.SetWordmarkHeaderFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Wide searchbar",
    description = "Adds an option to replace the search icon with a wide search bar. This will hide the YouTube logo when active.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class, AddResourcesPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39",
                "19.03.36",
                "19.04.38",
                "19.05.36",
                "19.06.39",
                "19.07.40",
                "19.08.36",
                "19.09.38",
                "19.10.39",
                "19.11.43"
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

    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/WideSearchbarPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.FEED.addPreferences(
            SwitchPreference("revanced_wide_searchbar")
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
    private fun BytecodeContext.walkMutable(index: Int, fromFingerprint: MethodFingerprint) =
        fromFingerprint.result?.let {
            toMethodWalker(it.method).nextMethod(index, true).getMethod() as MutableMethod
        } ?: throw fromFingerprint.exception


    /**
     * Injects instructions required for certain methods.
     */
    private fun MutableMethod.injectSearchBarHook() {
        val insertIndex = implementation!!.instructions.size - 1
        val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

        addInstructions(
            insertIndex,
            """
                invoke-static {v$insertRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->enableWideSearchbar(Z)Z
                move-result v$insertRegister
            """
        )
    }
}
