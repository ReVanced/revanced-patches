package app.revanced.patches.all.misc.network

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.debugging.enableAndroidDebuggingPatch
import app.revanced.util.Utils.trimIndentMultiline
import org.w3c.dom.Element
import java.io.File

@Suppress("unused")
val overrideCertificatePinningPatch = resourcePatch(
    name = "Override certificate pinning",
    description = "Overrides certificate pinning, allowing to inspect traffic via a proxy.",
    use = false,
) {
    dependsOn(enableAndroidDebuggingPatch)

    execute {
        val resXmlDirectory = get("res/xml")

        // Add android:networkSecurityConfig="@xml/network_security_config" and the "networkSecurityConfig" attribute if it does not exist.
        document("AndroidManifest.xml").use { document ->
            val applicationNode = document.getElementsByTagName("application").item(0) as Element

            if (!applicationNode.hasAttribute("networkSecurityConfig")) {
                document.createAttribute("android:networkSecurityConfig")
                    .apply { value = "@xml/network_security_config" }.let(applicationNode.attributes::setNamedItem)
            }
        }

        // In case the file does not exist create the "network_security_config.xml" file.
        File(resXmlDirectory, "network_security_config.xml").apply {
            writeText(
                """
                    <?xml version="1.0" encoding="utf-8"?>
                    <network-security-config>
                        <base-config cleartextTrafficPermitted="true">
                            <trust-anchors>
                                <certificates src="system" />
                                <certificates
                                    src="user"
                                    overridePins="true" />
                            </trust-anchors>
                        </base-config>
                        <debug-overrides>
                            <trust-anchors>
                                <certificates src="system" />
                                <certificates
                                    src="user"
                                    overridePins="true" />
                            </trust-anchors>
                        </debug-overrides>
                    </network-security-config>
                    """.trimIndentMultiline(),
            )
        }
    }
}
