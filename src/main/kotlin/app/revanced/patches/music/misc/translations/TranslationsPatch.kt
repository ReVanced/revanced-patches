package app.revanced.patches.music.misc.translations

import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.utils.settings.SettingsPatch
import app.revanced.patches.shared.patch.translations.AbstractTranslationsPatch

@Patch(
    name = "Translations",
    description = "Adds Crowdin translations for YouTube Music.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object TranslationsPatch  : AbstractTranslationsPatch(
    "music",
    arrayOf(
        "bg-rBG",
        "bn",
        "cs-rCZ",
        "el-rGR",
        "es-rES",
        "fr-rFR",
        "id-rID",
        "in",
        "it-rIT",
        "ja-rJP",
        "ko-rKR",
        "nl-rNL",
        "pl-rPL",
        "pt-rBR",
        "ro-rRO",
        "ru-rRU",
        "tr-rTR",
        "uk-rUA",
        "vi-rVN",
        "zh-rCN",
        "zh-rTW"
    )
)
