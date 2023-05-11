package app.revanced.patches.shared.settings.preference.impl

enum class InputType(val type: String) {
    STRING("text"), // TODO: rename to "TEXT"
    TEXT_CAP_CHARACTERS("textCapCharacters"),
    TEXT_MULTI_LINE("textMultiLine"),
    NUMBER("number"),
}