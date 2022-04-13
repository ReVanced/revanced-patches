package app.revanced.patches

import app.revanced.patcher.signature.MethodMetadata
import app.revanced.patcher.signature.PatcherMetadata
import app.revanced.patcher.signature.ResolverMethod
import app.revanced.patcher.signature.SignatureMetadata

val SHARED_METADATA = SignatureMetadata(
    MethodMetadata(null, null, ""),
    PatcherMetadata(ResolverMethod.Direct())
)