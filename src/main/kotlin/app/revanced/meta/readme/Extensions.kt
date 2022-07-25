package app.revanced.meta.readme

import app.revanced.patcher.data.Data
import app.revanced.patcher.extensions.PatchExtensions.compatiblePackages
import app.revanced.patcher.patch.Patch

internal fun Class<out Patch<Data>>.getLatestVersion(): SemanticVersion? =
    this.compatiblePackages?.first()?.versions?.map { SemanticVersion.fromString(it) }
        ?.maxWithOrNull(
            SemanticVersionComparator
        )
