package app.revanced.patches.shared.misc.gms

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.*
import app.revanced.patches.all.misc.packagename.changePackageNamePatch
import app.revanced.patches.all.misc.packagename.setOrGetFallbackPackageName
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.gms.Constants.ACTIONS
import app.revanced.patches.shared.misc.gms.Constants.AUTHORITIES
import app.revanced.patches.shared.misc.gms.Constants.PERMISSIONS
import app.revanced.util.exception
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference
import com.android.tools.smali.dexlib2.util.MethodUtil
import org.w3c.dom.Element
import org.w3c.dom.Node

private const val PACKAGE_NAME_REGEX_PATTERN = "^[a-z]\\w*(\\.[a-z]\\w*)+\$"

/**
 * A patch that allows patched Google apps to run without root and under a different package name
 * by using GmsCore instead of Google Play Services.
 *
 * @param fromPackageName The package name of the original app.
 * @param toPackageName The package name to fall back to if no custom package name is specified in patch options.
 * @param primeMethodFingerprint The fingerprint of the "prime" method that needs to be patched.
 * @param earlyReturnFingerprints The fingerprints of methods that need to be returned early.
 * @param mainActivityOnCreateFingerprint The fingerprint of the main activity onCreate method.
 * @param extensionPatch The patch responsible for the extension.
 * @param gmsCoreSupportResourcePatchFactory The factory for the corresponding resource patch
 * that is used to patch the resources.
 * @param executeBlock The additional execution block of the patch.
 * @param block The additional block to build the patch.
 */
fun gmsCoreSupportPatch(
    fromPackageName: String,
    toPackageName: String,
    primeMethodFingerprint: Fingerprint? = null,
    earlyReturnFingerprints: Set<Fingerprint> = setOf(),
    mainActivityOnCreateFingerprint: Fingerprint,
    extensionPatch: Patch<*>,
    gmsCoreSupportResourcePatchFactory: (gmsCoreVendorGroupIdOption: Option<String>) -> Patch<*>,
    executeBlock: Patch<BytecodePatchContext>.(BytecodePatchContext) -> Unit = {},
    block: BytecodePatchBuilder.() -> Unit = {},
) = bytecodePatch(
    name = "GmsCore support",
    description = "Allows patched Google apps to run without root and under a different package name " +
        "by using GmsCore instead of Google Play Services.",
) {
    val gmsCoreVendorGroupIdOption = stringOption(
        key = "gmsCoreVendorGroupId",
        default = "app.revanced",
        values =
        mapOf(
            "ReVanced" to "app.revanced",
        ),
        title = "GmsCore vendor group ID",
        description = "The vendor's group ID for GmsCore.",
        required = true,
    ) { it!!.matches(Regex(PACKAGE_NAME_REGEX_PATTERN)) }

    dependsOn(
        changePackageNamePatch,
        gmsCoreSupportResourcePatchFactory(gmsCoreVendorGroupIdOption),
        extensionPatch,
    )

    val gmsCoreVendorGroupId by gmsCoreVendorGroupIdOption

    val gmsCoreSupportMatch by gmsCoreSupportFingerprint()
    val mainActivityOnCreateMatch by mainActivityOnCreateFingerprint()
    primeMethodFingerprint?.invoke()
    googlePlayUtilityFingerprint()
    serviceCheckFingerprint()
    castDynamiteModuleFingerprint()
    earlyReturnFingerprints.forEach { it() }

    execute { context ->
        fun transformStringReferences(transform: (str: String) -> String?) = context.classes.forEach {
            val mutableClass by lazy {
                context.proxy(it).mutableClass
            }

            it.methods.forEach classLoop@{ method ->
                val implementation = method.implementation ?: return@classLoop

                val mutableMethod by lazy {
                    mutableClass.methods.first { MethodUtil.methodSignaturesMatch(it, method) }
                }

                implementation.instructions.forEachIndexed insnLoop@{ index, instruction ->
                    val string = ((instruction as? Instruction21c)?.reference as? StringReference)?.string
                        ?: return@insnLoop

                    // Apply transformation.
                    val transformedString = transform(string) ?: return@insnLoop

                    mutableMethod.replaceInstruction(
                        index,
                        BuilderInstruction21c(
                            Opcode.CONST_STRING,
                            instruction.registerA,
                            ImmutableStringReference(transformedString),
                        ),
                    )
                }
            }
        }

        // region Collection of transformations that are applied to all strings.

        fun commonTransform(referencedString: String): String? =
            when (referencedString) {
                "com.google",
                "com.google.android.gms",
                in PERMISSIONS,
                in ACTIONS,
                in AUTHORITIES,
                -> referencedString.replace("com.google", gmsCoreVendorGroupId!!)

                // No vendor prefix for whatever reason...
                "subscribedfeeds" -> "$gmsCoreVendorGroupId.subscribedfeeds"
                else -> null
            }

        fun contentUrisTransform(str: String): String? {
            // only when content:// uri
            if (str.startsWith("content://")) {
                // check if matches any authority
                for (authority in AUTHORITIES) {
                    val uriPrefix = "content://$authority"
                    if (str.startsWith(uriPrefix)) {
                        return str.replace(
                            uriPrefix,
                            "content://${authority.replace("com.google", gmsCoreVendorGroupId!!)}",
                        )
                    }
                }

                // gms also has a 'subscribedfeeds' authority, check for that one too
                val subFeedsUriPrefix = "content://subscribedfeeds"
                if (str.startsWith(subFeedsUriPrefix)) {
                    return str.replace(subFeedsUriPrefix, "content://$gmsCoreVendorGroupId.subscribedfeeds")
                }
            }

            return null
        }

        fun packageNameTransform(fromPackageName: String, toPackageName: String): (String) -> String? = { string ->
            when (string) {
                "$fromPackageName.SuggestionsProvider",
                "$fromPackageName.fileprovider",
                -> string.replace(fromPackageName, toPackageName)

                else -> null
            }
        }

        fun transformPrimeMethod(packageName: String) {
            primeMethodFingerprint!!.match?.mutableMethod?.apply {
                var register = 2

                val index = instructions.indexOfFirst {
                    if (it.getReference<StringReference>()?.string != fromPackageName) return@indexOfFirst false

                    register = (it as OneRegisterInstruction).registerA
                    return@indexOfFirst true
                }

                replaceInstruction(index, "const-string v$register, \"$packageName\"")
            } ?: throw primeMethodFingerprint.exception
        }

        // endregion

        val packageName = setOrGetFallbackPackageName(toPackageName)

        // Transform all strings using all provided transforms, first match wins.
        val transformations = arrayOf(
            ::commonTransform,
            ::contentUrisTransform,
            packageNameTransform(fromPackageName, packageName),
        )
        transformStringReferences transform@{ string ->
            transformations.forEach { transform ->
                transform(string)?.let { transformedString -> return@transform transformedString }
            }

            return@transform null
        }

        // Specific method that needs to be patched.
        primeMethodFingerprint?.let { transformPrimeMethod(packageName) }

        // Return these methods early to prevent the app from crashing.
        earlyReturnFingerprints.returnEarly()
        serviceCheckFingerprint.returnEarly()
        // Not all apps have CastDynamiteModule, so we need to check if it's present.
        if (castDynamiteModuleFingerprint.match != null) {
            castDynamiteModuleFingerprint.returnEarly()
        }
        // Google Play Utility is not present in all apps, so we need to check if it's present.
        if (googlePlayUtilityFingerprint.match != null) {
            googlePlayUtilityFingerprint.returnEarly()
        }

        // Verify GmsCore is installed and whitelisted for power optimizations and background usage.
        mainActivityOnCreateMatch.mutableMethod.apply {
            // Temporary fix for Google photos extension.
            val setContextIndex = indexOfFirstInstruction {
                val reference = getReference<MethodReference>() ?: return@indexOfFirstInstruction false

                reference.toString() == "Lapp/revanced/extension/shared/Utils;->setContext(Landroid/content/Context;)V"
            }

            // Add after setContext call, because this patch needs the context.
            addInstructions(
                if (setContextIndex < 0) 0 else setContextIndex + 1,
                "invoke-static/range { p0 .. p0 }, Lapp/revanced/extension/shared/GmsCoreSupport;->" +
                    "checkGmsCore(Landroid/app/Activity;)V",
            )
        }

        // Change the vendor of GmsCore in the extension.
        gmsCoreSupportMatch.mutableClass.methods
            .single { it.name == GET_GMS_CORE_VENDOR_GROUP_ID_METHOD_NAME }
            .replaceInstruction(0, "const-string v0, \"$gmsCoreVendorGroupId\"")

        executeBlock(context)
    }

    block()
}

/**
 * A collection of permissions, intents and content provider authorities
 * that are present in GmsCore which need to be transformed.
 *
 * NOTE: The following were present, but it seems like they are not needed to be transformed:
 * - com.google.android.gms.chimera.GmsIntentOperationService
 * - com.google.android.gms.phenotype.internal.IPhenotypeCallbacks
 * - com.google.android.gms.phenotype.internal.IPhenotypeService
 * - com.google.android.gms.phenotype.PACKAGE_NAME
 * - com.google.android.gms.phenotype.UPDATE
 * - com.google.android.gms.phenotype
 */
private object Constants {
    /**
     * A list of all permissions.
     */
    val PERMISSIONS = listOf(
        // C2DM / GCM
        "com.google.android.c2dm.permission.RECEIVE",
        "com.google.android.c2dm.permission.SEND",
        "com.google.android.gtalkservice.permission.GTALK_SERVICE",

        // GAuth
        "com.google.android.googleapps.permission.GOOGLE_AUTH",
        "com.google.android.googleapps.permission.GOOGLE_AUTH.cp",
        "com.google.android.googleapps.permission.GOOGLE_AUTH.local",
        "com.google.android.googleapps.permission.GOOGLE_AUTH.mail",
        "com.google.android.googleapps.permission.GOOGLE_AUTH.writely",
    )

    /**
     * All intent actions.
     */
    val ACTIONS = listOf(
        // location
        "com.google.android.gms.location.places.ui.PICK_PLACE",
        "com.google.android.gms.location.places.GeoDataApi",
        "com.google.android.gms.location.places.PlacesApi",
        "com.google.android.gms.location.places.PlaceDetectionApi",
        "com.google.android.gms.wearable.MESSAGE_RECEIVED",

        // C2DM / GCM
        "com.google.android.c2dm.intent.REGISTER",
        "com.google.android.c2dm.intent.REGISTRATION",
        "com.google.android.c2dm.intent.UNREGISTER",
        "com.google.android.c2dm.intent.RECEIVE",
        "com.google.iid.TOKEN_REQUEST",
        "com.google.android.gcm.intent.SEND",

        // car
        "com.google.android.gms.car.service.START",

        // people
        "com.google.android.gms.people.service.START",

        // wearable
        "com.google.android.gms.wearable.BIND",

        // auth
        "com.google.android.gsf.login",
        "com.google.android.gsf.action.GET_GLS",
        "com.google.android.gms.common.account.CHOOSE_ACCOUNT",
        "com.google.android.gms.auth.login.LOGIN",
        "com.google.android.gms.auth.api.credentials.PICKER",
        "com.google.android.gms.auth.api.credentials.service.START",
        "com.google.android.gms.auth.service.START",
        "com.google.firebase.auth.api.gms.service.START",
        "com.google.android.gms.auth.be.appcert.AppCertService",

        // fido
        "com.google.android.gms.fido.fido2.privileged.START",

        // gass
        "com.google.android.gms.gass.START",

        // games
        "com.google.android.gms.games.service.START",
        "com.google.android.gms.games.PLAY_GAMES_UPGRADE",

        // chimera
        "com.google.android.gms.chimera",

        // fonts
        "com.google.android.gms.fonts",

        // phenotype
        "com.google.android.gms.phenotype.service.START",

        // location
        "com.google.android.gms.location.reporting.service.START",

        // misc
        "com.google.android.gms.gmscompliance.service.START",
        "com.google.android.gms.oss.licenses.service.START",
        "com.google.android.gms.tapandpay.service.BIND",
        "com.google.android.gms.measurement.START",
        "com.google.android.gms.languageprofile.service.START",
        "com.google.android.gms.clearcut.service.START",
        "com.google.android.gms.icing.LIGHTWEIGHT_INDEX_SERVICE",
        "com.google.android.gms.accountsettings.action.VIEW_SETTINGS",

        // potoken
        "com.google.android.gms.potokens.service.START",

        // droidguard/ safetynet
        "com.google.android.gms.droidguard.service.START",
        "com.google.android.gms.safetynet.service.START",
    )

    /**
     * All content provider authorities.
     */
    val AUTHORITIES = listOf(
        // gsf
        "com.google.android.gsf.gservices",
        "com.google.settings",

        // auth
        "com.google.android.gms.auth.accounts",

        // chimera
        "com.google.android.gms.chimera",

        // fonts
        "com.google.android.gms.fonts",

        // phenotype
        "com.google.android.gms.phenotype",
    )
}

/**
 * Abstract resource patch that allows Google apps to run without root and under a different package name
 * by using GmsCore instead of Google Play Services.
 *
 * @param fromPackageName The package name of the original app.
 * @param toPackageName The package name to fall back to if no custom package name is specified in patch options.
 * @param spoofedPackageSignature The signature of the package to spoof to.
 * @param gmsCoreVendorGroupIdOption The option to get the vendor group ID of GmsCore.
 * @param executeBlock The additional execution block of the patch.
 * @param block The additional block to build the patch.
 */
fun gmsCoreSupportResourcePatch(
    fromPackageName: String,
    toPackageName: String,
    spoofedPackageSignature: String,
    gmsCoreVendorGroupIdOption: Option<String>,
    executeBlock: Patch<ResourcePatchContext>.(ResourcePatchContext) -> Unit = {},
    block: ResourcePatchBuilder.() -> Unit = {},
) = resourcePatch {
    dependsOn(
        changePackageNamePatch,
        addResourcesPatch,
    )

    val gmsCoreVendorGroupId by gmsCoreVendorGroupIdOption

    execute { context ->
        addResources("shared", "misc.gms.gmsCoreSupportResourcePatch")

        /**
         * Add metadata to manifest to support spoofing the package name and signature of GmsCore.
         */
        fun addSpoofingMetadata() {
            fun Node.adoptChild(
                tagName: String,
                block: Element.() -> Unit,
            ) {
                val child = ownerDocument.createElement(tagName)
                child.block()
                appendChild(child)
            }

            context.document["AndroidManifest.xml"].use { document ->
                val applicationNode =
                    document
                        .getElementsByTagName("application")
                        .item(0)

                // Spoof package name and signature.
                applicationNode.adoptChild("meta-data") {
                    setAttribute("android:name", "$gmsCoreVendorGroupId.android.gms.SPOOFED_PACKAGE_NAME")
                    setAttribute("android:value", fromPackageName)
                }

                applicationNode.adoptChild("meta-data") {
                    setAttribute("android:name", "$gmsCoreVendorGroupId.android.gms.SPOOFED_PACKAGE_SIGNATURE")
                    setAttribute("android:value", spoofedPackageSignature)
                }

                // GmsCore presence detection in extension.
                applicationNode.adoptChild("meta-data") {
                    // TODO: The name of this metadata should be dynamic.
                    setAttribute("android:name", "app.revanced.MICROG_PACKAGE_NAME")
                    setAttribute("android:value", "$gmsCoreVendorGroupId.android.gms")
                }
            }
        }

        /**
         * Patch the manifest to support GmsCore.
         */
        fun patchManifest() {
            val packageName = setOrGetFallbackPackageName(toPackageName)

            val transformations = mapOf(
                "package=\"$fromPackageName" to "package=\"$packageName",
                "android:authorities=\"$fromPackageName" to "android:authorities=\"$packageName",
                "$fromPackageName.permission.C2D_MESSAGE" to "$packageName.permission.C2D_MESSAGE",
                "$fromPackageName.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION" to "$packageName.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
                "com.google.android.c2dm" to "$gmsCoreVendorGroupId.android.c2dm",
                "com.google.android.libraries.photos.api.mars" to "$gmsCoreVendorGroupId.android.apps.photos.api.mars",
                "</queries>" to "<package android:name=\"$gmsCoreVendorGroupId.android.gms\"/></queries>",
            )

            val manifest = context["AndroidManifest.xml"]
            manifest.writeText(
                transformations.entries.fold(manifest.readText()) { acc, (from, to) ->
                    acc.replace(
                        from,
                        to,
                    )
                },
            )
        }

        patchManifest()
        addSpoofingMetadata()

        executeBlock(context)
    }

    block()
}
