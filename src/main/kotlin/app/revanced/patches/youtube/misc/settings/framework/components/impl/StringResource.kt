package app.revanced.patches.youtube.misc.settings.framework.components.impl

/**
 * Represents a string value in the strings.xml file
 *
 * @param name The name of the string
 * @param value The value of the string
 * @param formatted If the string is formatted. If false, the attribute will be set
 */
internal data class StringResource(val name: String, val value: String, val formatted: Boolean = true)
