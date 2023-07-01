package app.revanced.patches.reddit.customclients

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.patch.annotations.Patch

@Target(AnnotationTarget.CLASS)
@Patch
@Name("change-oauth-client-id")
annotation class ChangeOAuthClientIdPatchAnnotation