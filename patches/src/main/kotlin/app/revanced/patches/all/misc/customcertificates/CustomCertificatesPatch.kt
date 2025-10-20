package app.revanced.patches.all.misc.customcertificates

import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.booleanOption
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringsOption
import org.w3c.dom.Element
import java.io.File


private const val NSC_FILE_NAME_BARE = "network_security_config"
private const val RES_XML_DIR = "res/xml"
private const val RES_RAW_DIR = "res/raw"
private const val NSC_FILE_NAME_WITH_SUFFIX = "$NSC_FILE_NAME_BARE.xml"


val customNetworkSecurityPatch = resourcePatch(
    name = "Custom network security",
    description = "Allows trusting custom Certificate Authorities (CAs) for a specific domain.",
    use = false
) {

    val domains by stringsOption(
        key = "domains",
        title = "Target Domains",
        description = "List of domains to which the custom trust configuration will be applied (one domain per entry).",
        default = listOf("example.com"),
        required = true
    )

    val includeSubdomains by booleanOption(
        key = "includeSubdomains",
        title = "Include Subdomains",
        description = "Applies the configuration to all subdomains of the target domains.",
        default = false,
        required = true
    )

    val customCAFilePaths by stringsOption(
        key = "customCAFilePaths",
        title = "Custom Root CAs",
        description = """
            List of paths to files in PEM or DER format (one file path per entry).
                        
            Makes an app trust the provided custom Certificate Authorities (CAs),
            for the specified domains and their subdomains if the option "Include Subdomains" is enabled.
        
            CA files will be bundled in res/raw/ of resulting APK
        """.trimIndent(),
        default = null,
        required = false
    )

    val allowUserCerts by booleanOption(
        key = "allowUserCerts",
        title = "Trust User-Added CAs",
        description = "Makes an app trust certificates from the Android user store for the specified domains and their subdomains if the option \"Include Subdomains\" is enabled.",
        default = false,
        required = true
    )

    val allowSystemCerts by booleanOption(
        key = "allowSystemCerts",
        title = "Trust System CAs",
        description = "Makes an app trust certificates from the Android system store for the specified domains and their subdomains if the option \"Include Subdomains\" is enabled.",
        default = true,
        required = true
    )

    val allowCleartextTraffic by booleanOption(
        key = "allowCleartextTraffic",
        title = "Allow Cleartext Traffic (HTTP)",
        description = "Allows unencrypted HTTP traffic for the specified domains and their subdomains if the option \"Include Subdomains\" is enabled",
        default = false,
        required = true
    )

    val overridePins by booleanOption(
        key = "overridePins",
        title = "Override Certificate Pinning",
        description = "Overrides certificate pinning for the specified domains and their subdomains if the option \"Include Subdomains\" is enabled to allow inspecting a traffic via a proxy.",
        default = false,
        required = true
    )



    execute {
        document("AndroidManifest.xml").use { document ->
            val applicationNode =
                document
                    .getElementsByTagName("application")
                    .item(0) as Element

            applicationNode.setAttribute("android:networkSecurityConfig", "@xml/${NSC_FILE_NAME_BARE}")
        }

        val nscXmlContent = generateNetworkSecurityConfig(
            domains = domains ?: emptyList(),
            includeSubdomains = includeSubdomains ?: false,
            customCAFilePaths = customCAFilePaths ?: emptyList(),
            allowUser = allowUserCerts ?: false,
            allowSystem = allowSystemCerts ?: true,
            allowCleartextTraffic = allowCleartextTraffic ?: false,
            overridePins = overridePins ?: false
        )

        println(nscXmlContent)
        File(get(RES_XML_DIR), NSC_FILE_NAME_WITH_SUFFIX).apply {
            writeText(nscXmlContent)
        }

        println(customCAFilePaths)


        for (customCAFilePath in customCAFilePaths ?: emptyList()) {
            File(customCAFilePath).apply {
                if (!exists()) {
                    throw PatchException(
                        "The custom CA file path cannot be found: " +
                                absolutePath
                    )
                }

                if (!isFile) {
                    throw PatchException(
                        "The custom CA file path must be a file: "
                                + absolutePath
                    )
                }
            }
            val caFileNameWithoutSuffix = customCAFilePath.substringAfterLast('/').substringBefore('.')
            File(
                get(RES_RAW_DIR),
                caFileNameWithoutSuffix
            ).writeText(File(customCAFilePath).readText())

        }


    }
}

private fun generateNetworkSecurityConfig(
    domains: List<String>,
    includeSubdomains: Boolean,
    customCAFilePaths: List<String>,
    allowUser: Boolean,
    allowSystem: Boolean,
    allowCleartextTraffic: Boolean,
    overridePins: Boolean
): String {
    val domainsXMLString = StringBuilder()
    domains.forEachIndexed { index, domain ->
        println("^${domain}^") // a debug printl to see the entered domain string beginning and end with possible whitespaces
        val domainLine = """                <domain includeSubdomains="$includeSubdomains">$domain</domain>"""
        if (index < domains.lastIndex) {
            domainsXMLString.appendLine(domainLine)
        } else {
            domainsXMLString.append(domainLine)
        }
    }

    val trustAnchorsXMLString = StringBuilder()
    if (allowSystem) {
        trustAnchorsXMLString.appendLine()
        trustAnchorsXMLString.append("""                    <certificates src="system" overridePins="$overridePins" />""")
    }
    if (allowUser) {
        trustAnchorsXMLString.appendLine()
        trustAnchorsXMLString.append("""                    <certificates src="user" overridePins="$overridePins" />""")
    }

    for (caFilePath in customCAFilePaths) {
        val caFileNameWithoutSuffix = caFilePath.substringAfterLast('/').substringBeforeLast('.')
        trustAnchorsXMLString.appendLine()
        trustAnchorsXMLString.append("""                    <certificates src="@raw/$caFileNameWithoutSuffix" overridePins="$overridePins"/>""")
    }

    if (trustAnchorsXMLString.isBlank()) {
        throw PatchException("At least one trust anchor (System, User, or Custom CA) must be enabled.")
    }

    return """
        <?xml version="1.0" encoding="utf-8"?>
        <network-security-config>
            <domain-config cleartextTrafficPermitted="$allowCleartextTraffic">
$domainsXMLString
                <trust-anchors>$trustAnchorsXMLString
                </trust-anchors>
            </domain-config>
        </network-security-config>
    """.trimIndent()
}