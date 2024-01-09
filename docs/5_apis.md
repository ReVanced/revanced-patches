# 💪 Advanced APIs

[ReVanced](https://github.com/revanced/) comes with APIs which assist with the development of patches.

## 📙 Overview

1. 👹 Create mutable classes with `context.proxy(classDef)`
2. 🔍 Find mutable classes with `BytecodeContext.findClass(predicate)`
3. 🏃‍ Walk through the method call hierarchy with `BytecodeContext.toMethodWalker(startMethod)`
4. 🔨 Work with resources from patches with `ResourceUtils`
5. 💾 Read and write resources with `ResourceContext.get(path)`
6. 📃 Edit xml files with `DomFileEditor`
7. 🔧 Implement settings with `app.revanced.patches.shared.settings`

### 🧰 APIs

- #### 👹 Create mutable classes with `context.proxy(classDef)`

  To be able to make changes to classes, it is necessary to work on a mutable clone of that class.
  For that, the `BytecodeContext` allows to create mutable instances of classes with `context.proxy(classDef)`.

  Example:

  ```kt
  override fun execute(context: BytecodeContext) {   
    // Code
    
    val classProxy = context.proxy(someClass)

    // From now on, this class is shadowed over the original class.
    // The original class can still be found in context.classes.
    val proxy = classProxy.mutableClass 
  
    // Code
  
    classProxy.mutableClass.fields.add(someField)
  }
  ```
  > **Note**: The mutable clone will now be used for every future modification on the class, even in other patches,
  if `ClassProxy.mutableClass` is accessed. This means, if you try to proxy the same class twice, you will get the same
  instance of the mutable clone.

  > **Note**: On the page [🔎 Fingerprinting](3_fingerprinting.md) the result of fingerprints were introduced.
  Accessing [`MethodFingerprint.mutableClass`](https://github.com/revanced/revanced-patcher/blob/main/src/main/kotlin/app/revanced/patcher/fingerprint/method/impl/MethodFingerprint.kt#L290)
  or [`MutableFingerprint.mutableMethod`](https://github.com/revanced/revanced-patcher/blob/main/src/main/kotlin/app/revanced/patcher/fingerprint/method/impl/MethodFingerprint.kt#L298)
  also creates a mutable clone of the class through a `ClassProxy`.
  If you now were to proxy the same class, the fingerprint just proxied, the same mutable clone instance would be used.
  This also applies for fingerprints, which resolve to the same method.

  > **Warning**: Rely on the immutable types as much as possible to avoid creating mutable clones.

  An example on how this api is used can be found
  in [`GeneralAdsPatch`](https://github.com/revanced/revanced-patches/blob/f870178a77d4cb52e1940baa67aaa9526169d10d/src/main/kotlin/app/revanced/patches/reddit/ad/general/patch/GeneralAdsPatch.kt#L33).

- #### 🔍 Find mutable classes with `BytecodeContext.findClass(predicate)`

  This api allows to find classes by a predicate or the class name.
  It will return a `ClassProxy` instance by proxying the found class
  and thus either access the immutable or when necessary, the mutable clone of the class.

  An example on how this api is used can be found
  in [`HideCastButtonPatch`](https://github.com/revanced/revanced-patches/blob/0533e6c63e8da02f0b2b4df9652450c178255215/src/main/kotlin/app/revanced/patches/youtube/layout/castbutton/patch/HideCastButtonPatch.kt#L39).

