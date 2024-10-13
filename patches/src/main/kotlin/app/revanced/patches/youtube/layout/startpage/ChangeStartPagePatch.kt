package app.revanced.patches.youtube.layout.startpage

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.homeActivityFingerprint
import app.revanced.util.matchOrThrow

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/ChangeStartPagePatch;"

@Suppress("unused")
val changeStartPagePatch = bytecodePatch(
    name = "Change start page",
    description = "Adds an option to set which page the app opens in instead of the homepage.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube",
    )

    val homeActivityMatch by homeActivityFingerprint()

    execute { context ->
        addResources("youtube", "layout.startpage.changeStartPagePatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            ListPreference(
                key = "revanced_start_page",
                summaryKey = null,
            ),
        )

        startActivityFingerprint.apply {
            match(context, homeActivityMatch.classDef)
        }.matchOrThrow().mutableMethod.addInstruction(
            0,
            "invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->changeIntent(Landroid/content/Intent;)V",
        )
    }
}
