package app.revanced.patches.facebook.ads.mainfeed.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Annotation
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction31i
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue

internal object GraphQLStorySponsoredDataGetterFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, classDef ->
        // All methods within initialise a generic model with the same instructions apart from the value of those two constants
        classDef.type == "Lcom/facebook/graphql/model/GraphQLStory;" &&
                (methodDef.implementation?.instructions?.elementAt(1) as? Instruction31i)?.narrowLiteral == -132939024 &&
                (methodDef.implementation?.instructions?.elementAt(2) as? Instruction31i)?.narrowLiteral == 341202575
    },
)