package app.revanced.patches.youtube.layout.searchbar

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/WideSearchbarPatch;"

val wideSearchbarPatch = bytecodePatch(
    name = "Wide searchbar",
    description = "Adds an option to replace the search icon with a wide search bar. This will hide the YouTube logo when active.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.45.38",
            "19.46.42",
            "19.47.53",
        ),
    )

    execute {
        addResources("youtube", "layout.searchbar.wideSearchbarPatch")

        PreferenceScreen.FEED.addPreferences(
            SwitchPreference("revanced_wide_searchbar"),
        )

        /**
         * Navigate a fingerprints method at a given index mutably.
         *
         * @param index The index to navigate to.
         * @param from The fingerprint to navigate the method on.
         * @return The [MutableMethod] which was navigated on.
         */
        fun BytecodePatchContext.walkMutable(index: Int, from: Fingerprint) =
            navigate(from.originalMethod).to(index).stop()

        /**
         * Injects instructions required for certain methods.
         */
        fun MutableMethod.injectSearchBarHook() {
            val insertIndex = implementation!!.instructions.size - 1
            val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

            addInstructions(
                insertIndex,
                """
                invoke-static {v$insertRegister}, $EXTENSION_CLASS_DESCRIPTOR->enableWideSearchbar(Z)Z
                move-result v$insertRegister
            """,
            )
        }

        mapOf(
            setWordmarkHeaderFingerprint to 1,
            createSearchSuggestionsFingerprint to createSearchSuggestionsFingerprint.filterMatches.first().index,
        ).forEach { (fingerprint, callIndex) ->
            walkMutable(callIndex, fingerprint).injectSearchBarHook()
        }
    }
}
