package app.revanced.patches.youtube.misc.links

import app.revanced.patcher.*
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * 20.36 and lower.
 */
internal val abUriParserLegacyMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/Object;")
    parameterTypes("Ljava/lang/Object;")
    instructions(
        "Found entityKey=`"(),
        "that does not contain a PlaylistVideoEntityId"(String::contains),
        method { toString() == "Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;" },
    )
}

/**
 * 20.37+
 */
internal val abUriParserMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/Object;")
    parameterTypes("Ljava/lang/Object;")
    instructions(
        // Method is a switch statement of unrelated code,
        // and there's no strings or anything unique to fingerprint.
        method { toString() == "Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;" },
        method { toString() == "Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;" },
        method { toString() == "Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;" },
        method { toString() == "Ljava/util/List;->get(I)Ljava/lang/Object;" },
    )
}

internal val httpUriParserMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Landroid/net/Uri;")
    parameterTypes("Ljava/lang/String;")
    instructions(
        method { toString() == "Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;" },
        "https"(),
        "://"(),
        "https:"(),
    )
}
