package app.revanced.patches.music.layout.hide.general

import app.revanced.patches.music.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.shared.layout.hide.general.hideLayoutComponentsPatch

val hideLayoutComponentsPatch = hideLayoutComponentsPatch(
    lithoFilterPatch = lithoFilterPatch,
    settingsPatch = settingsPatch,
    filterClasses = setOf("Lapp/revanced/extension/shared/patches/components/CustomFilter;"),
    compatibleWithPackages = arrayOf("com.google.android.apps.youtube.music" to setOf("7.29.52", "8.10.52"))
)