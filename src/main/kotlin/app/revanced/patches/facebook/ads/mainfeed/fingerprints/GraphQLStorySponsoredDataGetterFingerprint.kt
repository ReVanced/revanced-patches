package app.revanced.patches.facebook.ads.mainfeed.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.facebook.ads.mainfeed.fingerprints.GraphQLStorySponsoredDataGetterFingerprint.SPONSORED_DATA_TYPE_ID
import app.revanced.patches.facebook.ads.mainfeed.fingerprints.GraphQLStorySponsoredDataGetterFingerprint.STORY_TYPE_ID
import com.android.tools.smali.dexlib2.iface.MethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction31i


internal object GraphQLStorySponsoredDataGetterFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, classDef ->
        fun isMatchingLiteralValue(index: Int, literal: Int): Boolean {
            val instruction = methodDef.implementation?.instructions?.elementAtOrNull(index) as? Instruction31i

            return instruction?.narrowLiteral == literal
        }

        // All methods within the target class are virtually identical apart from two magic constants on each of them.
        // They all return an instance of the same model but filled with different GraphQL child node data.
        // Likely, the constants are the parent and child node ids.
        classDef.type == "Lcom/facebook/graphql/model/GraphQLStory;" &&
                isMatchingLiteralValue(1, STORY_TYPE_ID) &&
                isMatchingLiteralValue(2, SPONSORED_DATA_TYPE_ID)
    }
) {
    // These constants trace back to some GraphQL node type descriptor.
    private const val STORY_TYPE_ID = -132939024
    private const val SPONSORED_DATA_TYPE_ID = 341202575
}
