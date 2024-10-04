package app.revanced.patches.facebook.ads.mainfeed.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.iface.MethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction31i

// These constants trace back to some GraphQL node type descriptor.
private const val STORY_TYPE_ID = -132939024
private const val SPONSORED_DATA_TYPE_ID = 341202575

internal object GraphQLStorySponsoredDataGetterFingerprint : MethodFingerprint(

    customFingerprint = { methodDef, classDef ->

        fun isMatchingLiteralValue(implementation: MethodImplementation?, index: Int, literal: Int): Boolean {
            return (implementation?.instructions?.elementAtOrNull(index) as? Instruction31i)?.narrowLiteral == literal
        }

        // All methods within the target class are virtually identical apart from two magic constants on each of them.
        // They all return an instance of the same model but filled with different GraphQL child node data.
        // Likely, the constants are the parent and child node ids.
        classDef.type == "Lcom/facebook/graphql/model/GraphQLStory;" &&
                isMatchingLiteralValue(methodDef.implementation, 1, STORY_TYPE_ID) &&
                isMatchingLiteralValue(methodDef.implementation, 2, SPONSORED_DATA_TYPE_ID)
    }
)