package app.revanced.patches.memegenerator.misc.pro

import app.revanced.patcher.extensions.replaceInstructions
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.memegenerator.detection.license.licenseValidationPatch
import app.revanced.patches.memegenerator.detection.signature.signatureVerificationPatch

@Suppress("unused", "ObjectPropertyName")
val `Unlock pro` by creatingBytecodePatch {
    dependsOn(signatureVerificationPatch, licenseValidationPatch)

    compatibleWith("com.zombodroid.MemeGenerator"("4.6364", "4.6370", "4.6375", "4.6377"))

    apply {
        isFreeVersionMethod.replaceInstructions(
            0,
            """
                sget-object p0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                return-object p0
            """,
        )
    }
}
