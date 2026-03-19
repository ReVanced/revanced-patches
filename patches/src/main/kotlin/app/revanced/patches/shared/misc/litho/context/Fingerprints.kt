package app.revanced.patches.shared.misc.litho.context

import app.revanced.patcher.gettingFirstImmutableMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal const val IDENTIFIER_PROPERTY = ", identifierProperty="


internal val BytecodePatchContext.conversionContextToStringMethod by gettingFirstImmutableMethodDeclaratively(
    ", widthConstraint=",
    ", heightConstraint=",
    ", templateLoggerFactory=",
    ", rootDisposableContainer=",
    IDENTIFIER_PROPERTY
) {
    name("toString")
    parameterTypes()
    returnType("Ljava/lang/String;")
    instructions("ConversionContext{"(String::contains))
}
