package app.revanced.meta.readme

class Template(template: String) {
    val result = StringBuilder(template)

    fun replaceVariable(regex: Regex, value: String) {
        val range = regex.find(result)!!.range
        result.replace(range.first, range.last + 1, value)
    }

    override fun toString(): String = result.toString()
}
