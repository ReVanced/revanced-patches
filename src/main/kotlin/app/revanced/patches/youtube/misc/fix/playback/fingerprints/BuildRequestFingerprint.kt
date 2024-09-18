package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object BuildRequestFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    returnType = "Lorg/chromium/net/UrlRequest;",
    customFingerprint = { methodDef, _ ->
        // Different targets have slightly different parameters

        // Earlier targets have parameters:
        //L
        //Ljava/util/Map;
        //[B
        //L
        //L
        //L
        //Lorg/chromium/net/UrlRequest$Callback;

        // Later targets have parameters:
        //L
        //Ljava/util/Map;
        //[B
        //L
        //L
        //L
        //Lorg/chromium/net/UrlRequest\$Callback;
        //L

        val parameterTypes = methodDef.parameterTypes
        (parameterTypes.size == 7 || parameterTypes.size == 8)
                && parameterTypes[1] == "Ljava/util/Map;" // URL headers.
    }
)
