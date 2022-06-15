package app.revanced.patches.youtube.layout.castbutton.signatures

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.signature.implementation.method.MethodSignature
import app.revanced.patcher.signature.implementation.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.signature.implementation.method.annotation.MatchingMethod
import app.revanced.patches.youtube.layout.castbutton.annotations.CastButtonCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("cast-button-signature")
@MatchingMethod(
    "Landroidx/mediarouter/app/MediaRouteButton", "setVisibility" // first one is prolly causing the issue
)
@FuzzyPatternScanMethod(2) // FIXME: Test this threshold and find the best value.
@CastButtonCompatibility
@Version("0.0.1")
object CastButtonSignature : MethodSignature(
    "V",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    listOf("L","L"),
    listOf(
                Opcode.IPUT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.RETURN_VOID,
    )
)
