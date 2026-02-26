package app.revanced.patches.tiktok.misc.share

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.PATCH_DESCRIPTION_SANITIZE_SHARING_LINKS
import app.revanced.patches.shared.PATCH_NAME_SANITIZE_SHARING_LINKS
import app.revanced.patches.tiktok.misc.extension.sharedExtensionPatch

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/tiktok/share/ShareUrlSanitizer;"

@Suppress("unused")
val sanitizeShareUrlsPatch = bytecodePatch(
    name = PATCH_NAME_SANITIZE_SHARING_LINKS,
    description = PATCH_DESCRIPTION_SANITIZE_SHARING_LINKS,
) {
    dependsOn(sharedExtensionPatch)

    compatibleWith(
        "com.ss.android.ugc.trill"("43.6.2"),
        "com.zhiliaoapp.musically"("43.6.2"),
    )

    execute {
        shareUrlShorteningFingerprint.method.addInstructions(
            0,
            """
                if-eqz p4, :revanced_skip_sanitization
                invoke-virtual {p4}, Ljava/lang/String;->length()I
                move-result v0
                if-eqz v0, :revanced_skip_sanitization

                invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->shouldSanitize()Z
                move-result v0
                if-eqz v0, :revanced_skip_sanitization

                invoke-static {p4}, $EXTENSION_CLASS_DESCRIPTOR->sanitizeShareUrl(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v0

                new-instance v1, LX/0rXE;
                invoke-direct {v1, v0}, LX/0rXE;-><init>(Ljava/lang/Object;)V
                return-object v1

                :revanced_skip_sanitization
                nop
            """,
        )
    }
}
