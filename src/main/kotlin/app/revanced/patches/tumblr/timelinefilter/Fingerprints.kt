package app.revanced.patches.tumblr.timelinefilter

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint.methodFingerprint

// This is the constructor of the PostsResponse class.
// The same applies here as with the TimelineConstructorFingerprint.
internal val postsResponseConstructorFingerprint = methodFingerprint {
    accessFlags(AccessFlags.CONSTRUCTOR, AccessFlags.PUBLIC)
    custom { methodDef, classDef -> classDef.endsWith("/PostsResponse;") && methodDef.parameters.size == 4 }
}

// This is the constructor of the Timeline class.
// It receives the List<TimelineObject> as an argument with a @Json annotation, so this should be the first time
// that the List<TimelineObject> is exposed in non-library code.
internal val timelineConstructorFingerprint = methodFingerprint {
    strings("timelineObjectsList")
    custom { methodDef, classDef ->
        classDef.endsWith("/Timeline;") && methodDef.parameters[0].type == "Ljava/util/List;"
    }
}

// This fingerprints the Integration TimelineFilterPatch.filterTimeline method.
// The opcode fingerprint is searching for
//   if ("BLOCKED_OBJECT_DUMMY".equals(elementType)) iterator.remove();
internal val timelineFilterIntegrationFingerprint = methodFingerprint {
    opcodes(
        Opcode.CONST_STRING, // "BLOCKED_OBJECT_DUMMY"
        Opcode.INVOKE_VIRTUAL, // HashSet.add(^)
    )
    strings("BLOCKED_OBJECT_DUMMY")
    custom { _, classDef ->
        classDef.endsWith("/TimelineFilterPatch;")
    }
}
