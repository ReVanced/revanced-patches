package app.revanced.patches.strava.mediaupload

import app.revanced.patcher.definingClass
import app.revanced.patcher.firstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.iface.ClassDef

context(_: BytecodePatchContext)
internal fun ClassDef.getGetCompressionQualityMethod() = firstMutableMethodDeclaratively {
    name("getCompressionQuality")
    definingClass { endsWith("/MediaUploadParameters;") }
}

context(_: BytecodePatchContext)
internal fun ClassDef.getGetMaxDurationMethod() = firstMutableMethodDeclaratively {
    name("getMaxDuration")
    definingClass { endsWith("/MediaUploadParameters;") }
}

context(_: BytecodePatchContext)
internal fun ClassDef.getGetMaxSizeMethod() = firstMutableMethodDeclaratively {
    name("getMaxSize")
    definingClass { endsWith("/MediaUploadParameters;") }
}
