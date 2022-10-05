package app.revanced.meta.readme

import app.revanced.patcher.data.Context
import app.revanced.patcher.extensions.PatchExtensions.compatiblePackages
import app.revanced.patcher.patch.Patch

internal fun Class<out Patch<Context>>.getLatestVersion() =
    this.compatiblePackages?.first()?.versions?.map {
        SemanticVersion.fromString(it)
    }?.maxWithOrNull(SemanticVersionComparator)
