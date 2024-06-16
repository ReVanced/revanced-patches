package app.revanced.patches.youtube.layout.startpage

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.homeActivityFingerprint
import app.revanced.util.resultOrThrow

private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/ChangeStartPagePatch;"

@Suppress("unused")
val changeStartPagePatch = bytecodePatch(
    name = "Change start page",
    description = "Adds an option to set which page the app opens in instead of the homepage.",
) {
    dependsOn(
        integrationsPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube",
    )

    val homeActivityFingerprintResult by homeActivityFingerprint()

    execute { context ->
        addResources("youtube", "layout.startpage.changeStartPagePatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            ListPreference(
                key = "revanced_start_page",
                summaryKey = null,
            ),
        )

        startActivityFingerprint.apply {
            resolve(context, homeActivityFingerprintResult.classDef)
        }.resultOrThrow().mutableMethod.addInstruction(
            0,
            "invoke-static { p1 }, $INTEGRATIONS_CLASS_DESCRIPTOR->changeIntent(Landroid/content/Intent;)V",
        )
    }
}
