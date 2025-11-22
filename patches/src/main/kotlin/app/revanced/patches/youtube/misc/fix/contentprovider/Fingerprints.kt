package app.revanced.patches.youtube.misc.fix.contentprovider

import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import app.revanced.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

internal val unstableContentProviderFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/content/ContentResolver;", "[Ljava/lang/String;")
    instructions(
        // Early targets use HashMap and later targets use ConcurrentMap.
        methodCall(
            name = "putAll",
            parameters = listOf("Ljava/util/Map;")
        ),
        string("ContentProvider query returned null cursor")
    )
}
