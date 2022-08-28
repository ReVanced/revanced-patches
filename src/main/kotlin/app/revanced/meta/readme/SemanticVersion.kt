package app.revanced.meta.readme

data class SemanticVersion(val major: Int, val minor: Int, val patch: Int) {
    companion object {
        fun fromString(version: String): SemanticVersion {
            val parts = version.split(".")
            if (parts.count() != 3) throw IllegalArgumentException("Invalid semantic version")
            val versionNumbers = parts.map { it.toInt() }
            return SemanticVersion(versionNumbers[0], versionNumbers[1], versionNumbers[2])
        }
    }

    override fun toString(): String = "$major.$minor.$patch"
}

object SemanticVersionComparator : Comparator<SemanticVersion> {
    override fun compare(a: SemanticVersion, b: SemanticVersion): Int = when {
        a.major != b.major -> a.major - b.major
        a.minor != b.minor -> a.minor - b.minor
        else -> a.patch - b.patch
    }
}
