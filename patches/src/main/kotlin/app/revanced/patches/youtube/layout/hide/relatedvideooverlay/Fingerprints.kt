package app.revanced.patches.youtube.layout.hide.relatedvideooverlay

import app.revanced.patcher.fingerprint
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import app.revanced.util.literal
import org.stringtemplate.v4.compiler.Bytecode.instructions

internal val relatedEndScreenResultsParentFingerprint by fingerprint {
    returns("V")
    instructions(
        resourceLiteral("layout", "app_related_endscreen_results")
    )
}

internal val relatedEndScreenResultsFingerprint by fingerprint {
    returns("V")
    parameters(
        "I",
        "Z",
        "I",
    )
}
