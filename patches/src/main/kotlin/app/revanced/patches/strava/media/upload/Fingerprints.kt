package app.revanced.patches.strava.media.upload

import app.revanced.patcher.definingClass
import app.revanced.patcher.firstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.iface.ClassDef

context(_: BytecodePatchContext)
internal fun ClassDef.getGetCompressionQualityMethod() = firstMethodDeclaratively {
    name("getCompressionQuality")
    definingClass("/MediaUploadParameters;")
}

context(_: BytecodePatchContext)
internal fun ClassDef.getGetMaxDurationMethod() = firstMethodDeclaratively {
    name("getMaxDuration")
    definingClass("/MediaUploadParameters;")
}

context(_: BytecodePatchContext)
internal fun ClassDef.getGetMaxSizeMethod() = firstMethodDeclaratively {
    name("getMaxSize")
    definingClass("/MediaUploadParameters;")
}
