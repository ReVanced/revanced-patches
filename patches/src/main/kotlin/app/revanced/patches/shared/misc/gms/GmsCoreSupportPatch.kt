package app.revanced.patches.shared.misc.gms

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.*
import app.revanced.patches.all.misc.packagename.changePackageNamePatch
import app.revanced.patches.all.misc.packagename.setOrGetFallbackPackageName
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.gms.Constants.APP_AUTHORITIES
import app.revanced.patches.shared.misc.gms.Constants.APP_PERMISSIONS
import app.revanced.patches.shared.misc.gms.Constants.GMS_AUTHORITIES
import app.revanced.patches.shared.misc.gms.Constants.GMS_PERMISSIONS
import app.revanced.util.*
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference
import com.android.tools.smali.dexlib2.util.MethodUtil
import java.net.URI

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/shared/GmsCoreSupport;"

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
    executeBlock: BytecodePatchContext.() -> Unit = {},
    block: BytecodePatchBuilder.() -> Unit = {},
) = bytecodePatch(
    name = "GmsCore support",
    description = "Allows the app to work without root by using a different package name when patched " +
        "using a GmsCore instead of Google Play Services.",
) {
    val gmsCoreVendorGroupIdOption = stringOption(
        key = "gmsCoreVendorGroupId",
        default = "app.revanced",
        values = mapOf("ReVanced" to "app.revanced"),
        title = "GmsCore vendor group ID",
        description = "The vendor's group ID for GmsCore.",
        required = true,
    ) { it!!.matches(Regex(PACKAGE_NAME_REGEX_PATTERN)) }

    dependsOn(
        changePackageNamePatch,
        gmsCoreSupportResourcePatchFactory(gmsCoreVendorGroupIdOption),
        extensionPatch,
    )

    execute {
        val gmsCoreVendorGroupId = gmsCoreVendorGroupIdOption.value!!

        fun transformStringReferences(transform: (str: String) -> String?) = classes.forEach {
            val mutableClass by lazy {
                proxy(it).mutableClass
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

        fun transformPackages(string: String): String? = when (string) {
            "com.google",
            "com.google.android.gms",
            in GMS_PERMISSIONS,
            in GMS_AUTHORITIES,
            -> if (string.startsWith("com.google")) {
                string.replace("com.google", gmsCoreVendorGroupId)
            } else {
                "$gmsCoreVendorGroupId.$string"
            }

            in APP_PERMISSIONS,
            in APP_AUTHORITIES,
            -> "$toPackageName.$string"

            else -> null
        }

        fun transformContentUrlAuthority(string: String) = if (!string.startsWith("content://")) {
            null
        } else {
            runCatching { URI.create(string) }.map {
                when (it.authority) {
                    in GMS_AUTHORITIES ->
                        if (it.authority.startsWith("com.google")) {
                            string.replace("com.google", gmsCoreVendorGroupId)
                        } else {
                            string.replace(
                                it.authority,
                                "$gmsCoreVendorGroupId.${it.authority}",
                            )
                        }

                    in APP_AUTHORITIES ->
                        string.replace(it.authority, "$toPackageName.${it.authority}")

                    else -> null
                }
            }.getOrNull()
        }

        val packageName = setOrGetFallbackPackageName(toPackageName)

        val transformations = arrayOf(
            ::transformPackages,
            ::transformContentUrlAuthority,
        )

        transformStringReferences transform@{ string ->
            transformations.forEach { transform ->
                transform(string)?.let { transformedString -> return@transform transformedString }
            }

            return@transform null
        }

        // Specific method that needs to be patched.
        if (primeMethodFingerprint?.methodOrNull != null) {
            val primeMethod = primeMethodFingerprint.method

            val index = primeMethod.indexOfFirstInstruction {
                getReference<StringReference>()?.string == fromPackageName
            }
            val register = primeMethod.getInstruction<OneRegisterInstruction>(index).registerA

            primeMethod.replaceInstruction(
                index,
                "const-string v$register, \"$packageName\"",
            )
        }

        // Return these methods early to prevent the app from crashing.
        earlyReturnFingerprints.forEach { it.method.returnEarly() }
        serviceCheckFingerprint.method.returnEarly()

        // Google Play Utility is not present in all apps, so we need to check if it's present.
        googlePlayUtilityFingerprint.methodOrNull?.returnEarly(0)

        // Set original and patched package names for extension to use.
        originalPackageNameExtensionFingerprint.method.returnEarly(fromPackageName)

        // Verify GmsCore is installed and whitelisted for power optimizations and background usage.
        mainActivityOnCreateFingerprint.method.addInstruction(
            0,
            "invoke-static/range { p0 .. p0 }, $EXTENSION_CLASS_DESCRIPTOR->" +
                "checkGmsCore(Landroid/app/Activity;)V",
        )

        // Change the vendor of GmsCore in the extension.
        getGmsCoreVendorGroupIdFingerprint.method.returnEarly(gmsCoreVendorGroupId)

        executeBlock()
    }

    block()
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
    executeBlock: ResourcePatchContext.() -> Unit = {},
    block: ResourcePatchBuilder.() -> Unit = {},
) = resourcePatch {
    dependsOn(
        changePackageNamePatch,
        addResourcesPatch,
    )

    val gmsCoreVendorGroupId = gmsCoreVendorGroupIdOption.value!!

    execute {
        addResources("shared", "misc.gms.gmsCoreSupportResourcePatch")

        document("AndroidManifest.xml").use { document ->
            document.getElementsByTagName("permission").asSequence().forEach { node ->
                val nameElement = node.attributes.getNamedItem("android:name")
                nameElement.textContent = toPackageName + nameElement.textContent
            }

            document.getElementsByTagName("uses-permission").asSequence().forEach { node ->
                val nameElement = node.attributes.getNamedItem("android:name")
                if (nameElement.textContent in GMS_PERMISSIONS) {
                    nameElement.textContent.replace("com.google", gmsCoreVendorGroupId)
                }
            }

            document.getElementsByTagName("provider").asSequence().forEach { node ->
                val providerElement = node.attributes.getNamedItem("android:authorities")

                providerElement.textContent = providerElement.textContent.split(";")
                    .joinToString(";") { authority ->
                        if (authority.startsWith("com.google")) {
                            authority.replace("com.google", gmsCoreVendorGroupId)
                        } else {
                            "$gmsCoreVendorGroupId.$authority"
                        }
                    }
            }

            document.getNode("manifest")
                .attributes.getNamedItem("package").textContent =
                setOrGetFallbackPackageName(toPackageName)

            document.getNode("queries").appendChild(
                document.createElement("package").apply {
                    attributes.setNamedItem(
                        document.createAttribute("android:name").apply {
                            textContent = "$gmsCoreVendorGroupId.android.gms"
                        },
                    )
                },
            )

            val applicationNode = document.getNode("application")

            // Spoof package name and signature.
            applicationNode.appendChild(
                document.createElement("meta-data").apply {
                    setAttribute(
                        "android:name",
                        "$gmsCoreVendorGroupId.android.gms.SPOOFED_PACKAGE_NAME",
                    )
                    setAttribute("android:value", fromPackageName)
                },
            )

            applicationNode.appendChild(
                document.createElement("meta-data").apply {
                    setAttribute(
                        "android:name",
                        "$gmsCoreVendorGroupId.android.gms.SPOOFED_PACKAGE_SIGNATURE",
                    )
                    setAttribute("android:value", spoofedPackageSignature)
                },
            )

            // GmsCore presence detection in extension.
            applicationNode.appendChild(
                document.createElement("meta-data").apply {
                    // TODO: The name of this metadata should be dynamic.
                    setAttribute("android:name", "app.revanced.MICROG_PACKAGE_NAME")
                    setAttribute("android:value", "$gmsCoreVendorGroupId.android.gms")
                },
            )
        }

        executeBlock()
    }

    block()
}

private object Constants {
    val GMS_PERMISSIONS = setOf(
        "com.google.android.providers.gsf.permission.READ_GSERVICES",
        "com.google.android.c2dm.permission.RECEIVE",
        "com.google.android.c2dm.permission.SEND",
        "com.google.android.gtalkservice.permission.GTALK_SERVICE",
        "com.google.android.googleapps.permission.GOOGLE_AUTH",
        "com.google.android.googleapps.permission.GOOGLE_AUTH.cp",
        "com.google.android.googleapps.permission.GOOGLE_AUTH.local",
        "com.google.android.googleapps.permission.GOOGLE_AUTH.mail",
        "com.google.android.googleapps.permission.GOOGLE_AUTH.writely",
        "com.google.android.gms.permission.ACTIVITY_RECOGNITION",
        "com.google.android.gms.permission.AD_ID",
        "com.google.android.gms.permission.AD_ID_NOTIFICATION",
        "com.google.android.gms.auth.api.phone.permission.SEND",
        "com.google.android.gms.permission.CAR_INFORMATION",
        "com.google.android.gms.permission.CAR_SPEED",
        "com.google.android.gms.permission.CAR_FUEL",
        "com.google.android.gms.permission.CAR_MILEAGE",
        "com.google.android.gms.permission.CAR_VENDOR_EXTENSION",
        "com.google.android.gms.locationsharingreporter.periodic.STATUS_UPDATE",
        "com.google.android.gms.auth.permission.GOOGLE_ACCOUNT_CHANGE",
    )

    val GMS_AUTHORITIES = setOf(
        "google.android.gms.fileprovider",
        "com.google.android.gms.auth.accounts",
        "com.google.android.gms.chimera",
        "com.google.android.gms.fonts",
        "com.google.android.gms.phenotype",
        "com.google.android.gsf.gservices",
        "com.google.settings",
        "subscribedfeeds",
    )

    val APP_PERMISSIONS = mutableSetOf<String>()

    val APP_AUTHORITIES = mutableSetOf<String>()
}
