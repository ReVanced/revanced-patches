# 🧩 Skeleton of a Patch

Patches are what make ReVanced, ReVanced. On the following page, the basic structure of a patch will be explained.

## ⛳️ Example patch

This page works with the following patch as an example:

```kt
package app.revanced.patches.ads.patch

// Imports

@Patch(
    name = "Disable Ads",
    description = "Disable ads.",
    dependsOn = [DisableAdResourcePatch::class],
   compatiblePackages = [CompatiblePackage("com.some.app", ["1.3.0"])]
)
object DisableAdsPatch : BytecodePatch(
    setOf(LoadAdsFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        val result = LoadAdsFingerprint.result
            ?: throw PatchException("LoadAdsFingerprint not found")

        result.mutableMethod.replaceInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )
    }
}
```

## 🔎 Dissecting the example patch

Let's start with understanding how a patch is structured. A patch is mainly built out of three components:

1. 📝 Patch annotations

   > Note:
   > A patch can either be instantiated using the `@Patch` annotation or the parameters of the super constructor.

   ```kt
   @Patch(
      name = "Disable Ads",
      description = "Disable ads.",
      dependsOn = [DisableAdResourcePatch::class],
      compatiblePackages = [CompatiblePackage("com.some.app", ["1.3.0"])]
   )
   ```

   Annotations are used to give context to the patch. They serve different but important purposes:

   - Every visible that sets `@Patch.name` will be loadable by `PatchBundleLoader` from the [introduction](1_introduction.md). 
     Patches that do not set `@Patch.name` can be referenced by other patches.
     We refer to those as _patch dependencies_. Patch dependencies are useful for structuring multiple patches.

     Example: _To add settings switches to an app, first, a patch is required that can provide a basic framework
     for other patches to add their toggles to that app. Those patches refer to the dependency patch
     and use its framework to add their toggles to an app. [ReVanced Patcher](https://github.com/revanced/revanced-patcher) will execute the dependency
     and the patch itself. When executed, the dependency can prepare a preference screen and then initialize itself
     for further use by other patches._

   - Patches may set `@Patch.description`.
     This annotation is used to describe the patch briefly.

   - Patches may set `@Patch.dependencies`.
     If the current patch depends on other patches, it can declare them as dependencies.

     Example: _The patch to remove ads needs to patch the bytecode.
     Additionally, it makes use of a second patch to get rid of resource files in the app that show ads in the app._

   - Patches may set `@Patch.compatiblePackages`.
     This annotation serves the purpose of constraining a patch to a package.
     Every patch is compatible with usually one or more packages.
     Additionally, the constraint may specify versions of the package it is guaranteed to be compatible with.

     Example: _The patch disables ads for an app.
     The app regularly updates, and the code of the app mutates heavily. In that case, the patch might not be compatible
with future untested app versions. To discourage the use of the app with versions other than the ones
this patch was confirmed to work on, it is constrained to those versions only._

   -  A patch may set `@Patch.requiresIntegrations` to true,
      if it depends on additional integrations to be merged by [ReVanced Patcher](https://github.com/revanced/revanced-patcher).
   
     > Integrations are precompiled classes that are useful for offloading and developing complex patches.
     Details of integrations and what exactly integrations are will be introduced properly on another page.

2. 🏗️ Patch class

   ```kt
   object DisableAdsPatch : BytecodePatch( /* Parameters */ ) {
     // ...
   }
   ```

   Usually, patches consist of a single object class.
   The class can be used to create methods and fields for the patch or provide a framework for other patches,
   in case it is meant to be used as a dependency patch.

   [ReVanced Patches](https://github.com/revanced/revanced-patches) follow a convention to name the class of patches:

   Example: _The class for a patch that disables ads should be called `DisableAdsPatch`,
   for a patch that adds a new download feature, it should be called `DownloadsPatch`._

   Each patch implicitly extends the [Patch](https://github.com/ReVanced/revanced-patcher/blob/67b7dff67a212b4fc30eb4f0cbe58f0ba09fb09a/revanced-patcher/src/main/kotlin/app/revanced/patcher/patch/BytecodePatch.kt#L27) class when extending off [ResourcePatch](https://github.com/revanced/revanced-patcher/blob/d2f91a8545567429d64a1bcad6ca1dab62ec95bf/src/main/kotlin/app/revanced/patcher/patch/Patch.kt#L35) or [BytecodePatch](https://github.com/revanced/revanced-patcher/blob/d2f91a8545567429d64a1bcad6ca1dab62ec95bf/src/main/kotlin/app/revanced/patcher/patch/Patch.kt#L42). The current example extends off `BytecodePatch`:

   ```kt
   object DisableAdsPatch : BytecodePatch( /* Parameters */ ) {
     // ...
   }
   ```

   If the patch extends off `ResourcePatch`, it can **patch resources** such as `XML`, `PNG` or similar files.
   On the other hand, if the patch extends off `BytecodePatch`, it can **patch the bytecode** of an app.
   If a patch needs access to the resources and the bytecode simultaneously.
   Either can use the other as a dependency.
   **Patches involving circular dependencies can not be added to a `Patcher` instance.**

3. 🏁 The `execute` method

   ```kt
   override fun execute(context: BytecodeContext) {
     // ...
   }
   ```

   The `execute` method is declared in the `Patch` class and, therefore, part of any patch:

   ```kt
   fun execute(context: /* Omitted */ T)
   ```

   It is the **first** method executed when running the patch.
   The current example extends off `BytecodePatch`. Since patches that extend on it can interact with the bytecode,
   the signature for the execute method when implemented requires a [BytecodeContext](https://github.com/ReVanced/revanced-patcher/blob/67b7dff67a212b4fc30eb4f0cbe58f0ba09fb09a/revanced-patcher/src/main/kotlin/app/revanced/patcher/data/BytecodeContext.kt) as a parameter:

   ```kt
   override fun execute(context: BytecodeContext) {
     // ...
   }
   ```

   The `BytecodeContext` contains everything necessary related to bytecode for patches,
   including every class of the app on which the patch will be applied.
   Likewise, a `ResourcePatch` will require a [ResourceContext](https://github.com/ReVanced/revanced-patcher/blob/67b7dff67a212b4fc30eb4f0cbe58f0ba09fb09a/revanced-patcher/src/main/kotlin/app/revanced/patcher/data/ResourceContext.kt)
   parameter and provide the patch with everything necessary to patch resources.

   Patches may throw `PatchException` if something goes wrong.
   If this patch is used as a dependency for other patches, those patches will not execute subsequently.

   In the current example, the `execute` method runs the following code to replace instructions at the index `0`
   of the methods instruction list:

   ```kt
   val result = LoadAdsFingerprint.result
     ?: throw PatchException("LoadAdsFingerprint not found")

   result.mutableMethod.replaceInstructions(
       0,
       """
           const/4 v0, 0x1
           return v0
       """
   )
   ```

> **Note**: Details of this implementation and what exactly `Fingerprints` are will be introduced properly on another page.

## 🤏 Minimal template for a bytecode patch

```kt
package app.revanced.patches.examples.minimal.patch

// Imports

@Patch(
    name = "Minimal Demonstration",
    description = "Demonstrates a minimal implementation of a patch.",
    compatiblePackages = [CompatiblePackage("com.some.app", ["1.3.0"])]
)
object MinimalExamplePatch : BytecodePatch() {
    override fun execute(context: BytecodeContext) =
        println("${MinimalExamplePatch.name} is being executed." )
}
```

## ⏭️ Whats next

The next section will explain how fingerprinting works.

Continue: [🔎 Fingerprinting](3_fingerprinting.md)
