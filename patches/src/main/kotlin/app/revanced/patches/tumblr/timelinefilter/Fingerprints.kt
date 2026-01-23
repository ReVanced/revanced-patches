package app.revanced.patches.tumblr.timelinefilter

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// This is the constructor of the PostsResponse class.
// The same applies here as with the TimelineConstructorMethod.
internal val BytecodePatchContext.postsResponseConstructorMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.CONSTRUCTOR, AccessFlags.PUBLIC)
    custom { method, classDef -> classDef.endsWith("/PostsResponse;") && method.parameters.size == 4 }
}

// This is the constructor of the Timeline class.
// It receives the List<TimelineObject> as an argument with a @Json annotation, so this should be the first time
// that the List<TimelineObject> is exposed in non-library code.
internal val BytecodePatchContext.timelineConstructorMethod by gettingFirstMethodDeclaratively {
    strings("timelineObjectsList")
    custom { method, classDef ->
        classDef.endsWith("/Timeline;") && method.parameters[0].type == "Ljava/util/List;"
    }
}

// This fingerprints the extension TimelineFilterPatch.filterTimeline method.
// The opcode fingerprint is searching for
//   if ("BLOCKED_OBJECT_DUMMY".equals(elementType)) iterator.remove();
internal val BytecodePatchContext.timelineFilterExtensionMethod by gettingFirstMethodDeclaratively {
    opcodes(
        Opcode.CONST_STRING, // "BLOCKED_OBJECT_DUMMY"
        Opcode.INVOKE_VIRTUAL, // HashSet.add(^)
    )
    strings("BLOCKED_OBJECT_DUMMY")
    custom { _, classDef ->
        classDef.endsWith("/TimelineFilterPatch;")
    }
}
