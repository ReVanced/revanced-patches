package app.revanced.patches.reddit.customclients.sync.syncforreddit.api

import app.revanced.patcher.ClassDefComposing
import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.string
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.getAuthorizationStringMethodMatch by composingFirstMethod {
    instructions(string { contains("authorize.compact?client_id") })
}

internal val ClassDef.getBearerTokenMethodMatch by ClassDefComposing.composingFirstMethod {
    instructions(string { contains("Basic") })
}

internal val BytecodePatchContext.getUserAgentMethod by gettingFirstMethodDeclaratively {
    instructions("android:com.laurencedawson.reddit_sync"(String::contains))
}

internal val BytecodePatchContext.imgurImageAPIMethodMatch by composingFirstMethod {
    instructions("https://imgur-apiv3.p.rapidapi.com/3/image"())
}
