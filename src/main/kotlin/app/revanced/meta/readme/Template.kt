package app.revanced.meta.readme

class Template(template: String) {
    val result: StringBuilder = StringBuilder(template)

    fun replaceVariable(name: String, value: String) {
        val regex = Regex("\\{\\{\\s?$name\\s?}}")
        val range = regex.find(result)!!.range

        result.replace(range.first, range.last + 1, value)
    }

    override fun toString(): String = result.toString()
}
