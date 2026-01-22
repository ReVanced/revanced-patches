package app.revanced.patches.trakt

import app.revanced.patcher.custom
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.isVIPEPMethod by gettingFirstMutableMethodDeclaratively {
    custom { method, classDef ->
        if (!classDef.endsWith("RemoteUser;")) return@custom false

        method.name == "isVIPEP"
    }
}

internal val BytecodePatchContext.isVIPMethod by gettingFirstMutableMethodDeclaratively {
    custom { method, classDef ->
        if (!classDef.endsWith("RemoteUser;")) return@custom false

        method.name == "isVIP"
    }
}
internal val BytecodePatchContext.remoteUserMethod by gettingFirstMutableMethodDeclaratively {
    custom { _, classDef ->
        classDef.endsWith("RemoteUser;")
    }
}

// TODO