package app.revanced.patches.all.misc.customcertificates

import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.booleanOption
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringsOption
import app.revanced.util.Utils.trimIndentMultiline
import app.revanced.util.getNode
import org.w3c.dom.Element
import java.io.File




val customNetworkSecurityPatch = resourcePatch(
    name = "Custom network security",
    description = "Allows trusting custom certificate authorities for a specific domain.",
    use = false
) {

    val targetDomains by stringsOption(
        key = "targetDomains",
        title = "Target domains",
        description = "List of domains to which the custom trust configuration will be applied (one domain per entry).",
        default = listOf("example.com"),
        required = true
    )

    val includeSubdomains by booleanOption(
        key = "includeSubdomains",
        title = "Include subdomains",
        description = "Applies the configuration to all subdomains of the target domains.",
        default = false,
        required = true
    )

    val customCAFilePaths by stringsOption(
        key = "customCAFilePaths",
        title = "Custom CA file paths",
        description = """
            List of paths to files in PEM or DER format (one file path per entry).
                        
            Makes an app trust the provided custom certificate authorities (CAs),
            for the specified domains, and if the option "Include Subdomains" is enabled then also the subdomains.

        
            CA files will be bundled in res/raw/ of resulting APK
        """.trimIndentMultiline(),
        default = null,
        required = false
    )

    val allowUserCerts by booleanOption(
        key = "allowUserCerts",
        title = "Trust user added CAs",
        description = "Makes an app trust certificates from the Android user store for the specified domains, and if the option \"Include Subdomains\" is enabled then also the subdomains.",

        default = false,
        required = true
    )

    val allowSystemCerts by booleanOption(
        key = "allowSystemCerts",
        title = "Trust system CAs",
        description = "Makes an app trust certificates from the Android system store for the specified domains, and and if the option \"Include Subdomains\" is enabled then also the subdomains.",

        default = true,
        required = true
    )

    val allowCleartextTraffic by booleanOption(
        key = "allowCleartextTraffic",
        title = "Allow cleartext traffic (HTTP)",
        description = "Allows unencrypted HTTP traffic for the specified domains, and if \"Include Subdomains\" is enabled then also the subdomains.",

        default = false,
        required = true
    )

    val overridePins by booleanOption(
        key = "overridePins",
        title = "Override certificate pinning",
        description = "Overrides certificate pinning for the specified domains and their subdomains if the option \"Include Subdomains\" is enabled to allow inspecting app traffic via a proxy.",

        default = false,
        required = true
    )

    fun generateNetworkSecurityConfig(): String {
        val targetDomains = targetDomains ?: emptyList()
        val includeSubdomains = includeSubdomains ?: false
        val customCAFilePaths = customCAFilePaths ?: emptyList()
        val allowUserCerts = allowUserCerts ?: false
        val allowSystemCerts = allowSystemCerts ?: true
        val allowCleartextTraffic = allowCleartextTraffic ?: false
        val overridePins = overridePins ?: false

        val domainsXML = buildString {
            targetDomains.forEach {
                appendLine("""                <domain includeSubdomains="$includeSubdomains">$it</domain>""")
            }
        }.trimEnd()

        val trustAnchorsXML = buildString {
            if (allowSystemCerts) {
                appendLine("""                    <certificates src="system" overridePins="$overridePins" />""")
            }
            if (allowUserCerts) {
                appendLine("""                    <certificates src="user" overridePins="$overridePins" />""")
            }
            customCAFilePaths.forEach { path ->
                val fileName = path.substringAfterLast('/').substringBeforeLast('.')
                appendLine("""                    <certificates src="@raw/$fileName" overridePins="$overridePins" />""")
            }
        }

        if (trustAnchorsXML.isBlank()) {
            throw PatchException("At least one trust anchor (System, User, or Custom CA) must be enabled.")
        }

        return """
        <?xml version="1.0" encoding="utf-8"?>
        <network-security-config>
            <domain-config cleartextTrafficPermitted="$allowCleartextTraffic">
$domainsXML
                <trust-anchors>
${trustAnchorsXML.trimEnd()}
                </trust-anchors>
            </domain-config>
        </network-security-config>
    """.trimIndent()
    }


    execute {
        val nscFileNameBare = "network_security_config"
        val resXmlDir = "res/xml"
        val resRawDir = "res/raw"
        val nscFileNameWithSuffix = "$nscFileNameBare.xml"


        document("AndroidManifest.xml").use { document ->
            val applicationNode = document.getNode("application") as Element
            applicationNode.setAttribute("android:networkSecurityConfig", "@xml/$nscFileNameBare")
        }


        File(get(resXmlDir), nscFileNameWithSuffix).apply {
            writeText(generateNetworkSecurityConfig())
        }



        for (customCAFilePath in customCAFilePaths ?: emptyList()) {
            val file = File(customCAFilePath)
            if (!file.exists()) {
                throw PatchException(
                    "The custom CA file path cannot be found: " +
                            file.absolutePath
                )
            }

            if (!file.isFile) {
                throw PatchException(
                    "The custom CA file path must be a file: "
                            + file.absolutePath
                )
            }
            val caFileNameWithoutSuffix = customCAFilePath.substringAfterLast('/').substringBefore('.')
            val caFile = File(customCAFilePath)
            File(
                get(resRawDir),
                caFileNameWithoutSuffix
            ).writeText(
                caFile.readText()
            )

        }


    }
}
