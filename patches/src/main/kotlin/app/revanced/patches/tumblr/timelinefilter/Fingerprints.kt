package app.revanced.patches.tumblr.timelinefilter

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// This is the constructor of the PostsResponse class.
// The same applies here as with the TimelineConstructorMethod.
internal val BytecodePatchContext.postsResponseConstructorMethod by gettingFirstMethodDeclaratively {
    definingClass { endsWith("/PostsResponse;") }
    accessFlags(AccessFlags.CONSTRUCTOR, AccessFlags.PUBLIC)
    custom { parameters.size == 4 }
}

// This is the constructor of the Timeline class.
// It receives the List<TimelineObject> as an argument with a @Json annotation, so this should be the first time
// that the List<TimelineObject> is exposed in non-library code.
internal val BytecodePatchContext.timelineConstructorMethod by gettingFirstMethodDeclaratively("timelineObjectsList") {
    definingClass { endsWith("/Timeline;") }
    custom { parameters[0].type == "Ljava/util/List;" }
}

// This gets the extension method TimelineFilterPatch.filterTimeline.
// Looking for
//   if ("BLOCKED_OBJECT_DUMMY".equals(elementType)) iterator.remove();
internal val BytecodePatchContext.timelineFilterExtensionMethodMatch by composingFirstMethod("BLOCKED_OBJECT_DUMMY") {
    definingClass { endsWith("/TimelineFilterPatch;") }
    opcodes(
        Opcode.CONST_STRING, // "BLOCKED_OBJECT_DUMMY"
        Opcode.INVOKE_VIRTUAL, // HashSet.add(^)
    )
}
