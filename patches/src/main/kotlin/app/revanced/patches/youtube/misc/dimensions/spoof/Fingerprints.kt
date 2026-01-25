package app.revanced.patches.youtube.misc.dimensions.spoof

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext

// Strings are partial matches (format delimiters), so keep in instructions block.
internal val deviceDimensionsModelToStringMethodMatch = firstMethodComposite {
    returnType("L")
    strings {
        +"minh."
        +";maxh."
    }
}
