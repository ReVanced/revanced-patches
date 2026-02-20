package app.revanced.patches.gamehub.network

import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.asSequence

@Suppress("unused")
val networkSecurityPatch = resourcePatch(
    name = "Trust user certificates",
    description = "Adds user-installed CA certificates to the network security trust anchors.",
) {
    compatibleWith("com.xiaoji.egggame"("5.3.5"))

    execute {
        document("res/xml/network_security_config.xml").use { dom ->
            val root = dom.documentElement

            // Ensure <base-config> exists.
            val baseConfig = dom.getElementsByTagName("base-config").let { nodes ->
                if (nodes.length > 0) nodes.item(0)
                else dom.createElement("base-config").also { root.appendChild(it) }
            }

            // Collect all config elements that need user trust anchors:
            // <base-config> and any <domain-config> blocks.
            val configs = buildList {
                add(baseConfig)
                dom.getElementsByTagName("domain-config").asSequence().forEach { add(it) }
            }

            for (config in configs) {
                // Find or create <trust-anchors> inside this config element.
                val trustAnchors = config.childNodes.asSequence()
                    .firstOrNull { it.nodeName == "trust-anchors" }
                    ?: dom.createElement("trust-anchors").also { config.appendChild(it) }

                // Add <certificates src="user"> if not already present.
                val hasUser = trustAnchors.childNodes.asSequence().any {
                    it.nodeName == "certificates" &&
                        it.attributes?.getNamedItem("src")?.nodeValue == "user"
                }

                if (!hasUser) {
                    dom.createElement("certificates").apply {
                        setAttribute("src", "user")
                    }.let(trustAnchors::appendChild)
                }
            }
        }
    }
}
