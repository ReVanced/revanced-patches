package app.revanced.patches.cieid.restrictions.root

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.checkRootMethod by gettingFirstMutableMethodDeclaratively {
    name("onResume")
    definingClass("Lit/ipzs/cieid/BaseActivity;")
}
