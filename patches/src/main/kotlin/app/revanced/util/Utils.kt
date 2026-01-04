package app.revanced.util

internal object Utils {
    internal fun String.trimIndentMultiline() =
        this.split("\n")
            .joinToString("\n") { it.trimIndent() } // Remove the leading whitespace from each line.
            .trimIndent() // Remove the leading newline.
}

internal fun Boolean.toHexString(): String = if (this) "0x1" else "0x0"

internal fun Class<*>.allAssignableTypes(): Set<Class<*>> {
    val result = mutableSetOf<Class<*>>()

    fun visit(child: Class<*>?) {
        if (child == null || !result.add(child)) {
            return
        }

        child.interfaces.forEach(::visit)
        visit(child.superclass)
    }

    visit(this)

    return result
}
