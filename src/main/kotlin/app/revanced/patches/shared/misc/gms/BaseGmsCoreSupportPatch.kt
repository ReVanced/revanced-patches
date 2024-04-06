package app.revanced.patches.shared.misc.gms

import app.revanced.patcher.PatchClass
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.all.misc.packagename.ChangePackageNamePatch
import app.revanced.patches.shared.misc.gms.BaseGmsCoreSupportPatch.Constants.ACTIONS
import app.revanced.patches.shared.misc.gms.BaseGmsCoreSupportPatch.Constants.AUTHORITIES
import app.revanced.patches.shared.misc.gms.BaseGmsCoreSupportPatch.Constants.PERMISSIONS
import app.revanced.patches.shared.misc.gms.fingerprints.GmsCoreSupportFingerprint
import app.revanced.patches.shared.misc.gms.fingerprints.GmsCoreSupportFingerprint.GET_GMS_CORE_VENDOR_GROUP_ID_METHOD_NAME
import app.revanced.util.exception
import app.revanced.util.getReference
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference
import com.android.tools.smali.dexlib2.util.MethodUtil

/**
 * A patch that allows patched Google apps to run without root and under a different package name
 * by using GmsCore instead of Google Play Services.
 *
 * @param fromPackageName The package name of the original app.
 * @param toPackageName The package name to fall back to if no custom package name is specified in patch options.
 * @param primeMethodFingerprint The fingerprint of the "prime" method that needs to be patched.
 * @param earlyReturnFingerprints The fingerprints of methods that need to be returned early.
 * @param mainActivityOnCreateFingerprint The fingerprint of the main activity onCreate method.
 * @param integrationsPatchDependency The patch responsible for the integrations.
 * @param gmsCoreSupportResourcePatch The corresponding resource patch that is used to patch the resources.
 * @param dependencies Additional dependencies of this patch.
 * @param compatiblePackages The compatible packages of this patch.
 * @param fingerprints The fingerprints of this patch.
 */
abstract class BaseGmsCoreSupportPatch(
    private val fromPackageName: String,
    private val toPackageName: String,
    private val primeMethodFingerprint: MethodFingerprint,
    private val earlyReturnFingerprints: Set<MethodFingerprint>,
    private val mainActivityOnCreateFingerprint: MethodFingerprint,
    private val integrationsPatchDependency: PatchClass,
    gmsCoreSupportResourcePatch: BaseGmsCoreSupportResourcePatch,
    dependencies: Set<PatchClass> = setOf(),
    compatiblePackages: Set<CompatiblePackage>? = null,
    fingerprints: Set<MethodFingerprint> = emptySet(),
) : BytecodePatch(
    name = "GmsCore support",
    description = "Allows patched Google apps to run without root and under a different package name " +
        "by using GmsCore instead of Google Play Services.",
    dependencies = setOf(
        ChangePackageNamePatch::class,
        gmsCoreSupportResourcePatch::class,
        integrationsPatchDependency,
    ) + dependencies,
    compatiblePackages = compatiblePackages,
    fingerprints = setOf(
        GmsCoreSupportFingerprint,
        mainActivityOnCreateFingerprint,
    ) + fingerprints,
    requiresIntegrations = true,
) {
    init {
        // Manually register all options of the resource patch so that they are visible in the patch API.
        gmsCoreSupportResourcePatch.options.values.forEach(options::register)
    }

    internal abstract val gmsCoreVendorGroupId: String?

    override fun execute(context: BytecodeContext) {
        val packageName = ChangePackageNamePatch.setOrGetFallbackPackageName(toPackageName)

        // Transform all strings using all provided transforms, first match wins.
        val transformations = arrayOf(
            ::commonTransform,
            ::contentUrisTransform,
            packageNameTransform(fromPackageName, packageName),
        )
        context.transformStringReferences transform@{ string ->
            transformations.forEach { transform ->
                transform(string)?.let { transformedString -> return@transform transformedString }
            }

            return@transform null
        }

        // Specific method that needs to be patched.
        transformPrimeMethod(packageName)

        // Return these methods early to prevent the app from crashing.
        earlyReturnFingerprints.toList().returnEarly()

        // Verify GmsCore is installed and whitelisted for power optimizations and background usage.
        mainActivityOnCreateFingerprint.result?.mutableMethod?.addInstructions(
            0,
            "invoke-static/range { p0 .. p0 }, Lapp/revanced/integrations/shared/GmsCoreSupport;->" +
                "checkGmsCore(Landroid/app/Activity;)V",
        ) ?: throw mainActivityOnCreateFingerprint.exception

        // Change the vendor of GmsCore in ReVanced Integrations.
        GmsCoreSupportFingerprint.result?.mutableClass?.methods
            ?.single { it.name == GET_GMS_CORE_VENDOR_GROUP_ID_METHOD_NAME }
            ?.replaceInstruction(0, "const-string v0, \"$gmsCoreVendorGroupId\"")
            ?: throw GmsCoreSupportFingerprint.exception
    }

    private fun BytecodeContext.transformStringReferences(transform: (str: String) -> String?) = classes.forEach {
        val mutableClass by lazy {
            proxy(it).mutableClass
        }

        it.methods.forEach classLoop@{ methodDef ->
            val implementation = methodDef.implementation ?: return@classLoop

            val mutableMethod by lazy {
                mutableClass.methods.first { MethodUtil.methodSignaturesMatch(it, methodDef) }
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

    private fun commonTransform(referencedString: String): String? =
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

    private fun contentUrisTransform(str: String): String? {
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

    private fun packageNameTransform(fromPackageName: String, toPackageName: String): (String) -> String? = { string ->
        when (string) {
            "$fromPackageName.SuggestionsProvider",
            "$fromPackageName.fileprovider",
            -> string.replace(fromPackageName, toPackageName)

            else -> null
        }
    }

    private fun transformPrimeMethod(packageName: String) {
        primeMethodFingerprint.result?.mutableMethod?.apply {
            var register = 2

            val index = getInstructions().indexOfFirst {
                if (it.getReference<StringReference>()?.string != fromPackageName) return@indexOfFirst false

                register = (it as OneRegisterInstruction).registerA
                return@indexOfFirst true
            }

            replaceInstruction(index, "const-string v$register, \"$packageName\"")
        } ?: throw primeMethodFingerprint.exception
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

    // endregion
}
