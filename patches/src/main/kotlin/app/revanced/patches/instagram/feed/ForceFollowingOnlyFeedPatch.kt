package app.revanced.patches.instagram.feed

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.instagram.misc.extension.sharedExtensionPatch

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/instagram/feed/ForceFollowingOnlyFeedPatch;"

@Suppress("unused")
val setFollowerOnlyHomePatch = bytecodePatch(
    name = "Force following only feed",
    description = "The home feed will contains only posts/reels of people you are following.",
) {
    compatibleWith("com.instagram.android")

    dependsOn(sharedExtensionPatch)

    execute {
        val initRequestMethodFingerprint = fingerprint {
            custom { method, classDef ->
                method.name == "<init>"
                        && classDef == mainFeedRequestFingerprint.classDef
            }

        }

        initRequestMethodFingerprint.method.addInstructions(
            0,
            """
                   invoke-static/range {p16 .. p16}, $EXTENSION_CLASS_DESCRIPTOR->setFollowersHeader(Ljava/util/Map;)Ljava/util/Map;
                   move-result-object p16
                   """
        )
    }
}
