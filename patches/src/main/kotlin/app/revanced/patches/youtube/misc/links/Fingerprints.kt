package app.revanced.patches.youtube.misc.links

import app.revanced.patcher.StringComparisonType
import app.revanced.patcher.accessFlags
import app.revanced.patcher.addString
import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.methodCall
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.returnType
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
        addString("that does not contain a PlaylistVideoEntityId", comparison = StringComparisonType.CONTAINS),
        methodCall(smali = "Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;"),
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
        methodCall(smali = "Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;"),
        methodCall(smali = "Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;"),
        methodCall(smali = "Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;"),
        methodCall(smali = "Ljava/util/List;->get(I)Ljava/lang/Object;"),
    )
}

internal val httpUriParserMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Landroid/net/Uri;")
    parameterTypes("Ljava/lang/String;")
    instructions(
        methodCall(smali = "Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;"),
        "https"(),
        "://"(),
        "https:"(),
    )
}
