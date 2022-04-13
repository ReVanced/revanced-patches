package app.revanced.signatures

import app.revanced.patcher.signature.MethodSignature

interface SignatureSupplier {
    fun get(): MethodSignature
}