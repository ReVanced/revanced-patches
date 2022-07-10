package app.revanced.patches.meta.readme

class Template(val template: String) {
    val result: StringBuilder = StringBuilder(template)

    fun replaceVariable(name: String, value: String) {
        val regex = Regex("\\{\\{\\s?$name\\s?\\}\\}")
        val range = regex.find(result)!!.range

        result.replace(range.start, range.endInclusive + 1, value)
    }

    override fun toString(): String = result.toString()
}
