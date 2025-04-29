package app.revanced.patches.shared.extension

@Suppress("MemberVisibilityCanBePrivate")
internal object Constants {
    const val EXTENSION_PATH = "Lapp/revanced/extension/shared"
    const val PATCHES_PATH = "$EXTENSION_PATH/patches"
    const val COMPONENTS_PATH = "$PATCHES_PATH/components"
    const val SPANS_PATH = "$PATCHES_PATH/spans"
    const val SPOOF_PATH = "$PATCHES_PATH/spoof"

    const val EXTENSION_UTILS_PATH = "$EXTENSION_PATH/utils"
    const val EXTENSION_SETTING_CLASS_DESCRIPTOR = "$EXTENSION_PATH/settings/Setting;"
    const val EXTENSION_UTILS_CLASS_DESCRIPTOR = "$EXTENSION_UTILS_PATH/Utils;"
}