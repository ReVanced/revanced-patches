package app.revanced.patches.youtube.misc.mapping.patch

import app.revanced.extensions.doRecursively
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.implementation.ResourceData
import app.revanced.patcher.patch.implementation.ResourcePatch
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import org.w3c.dom.Element

@Name("resource-id-mapping-provider-resource-patch-dependency")
@Description("This patch works as a acts as a provider for resources mapped to their ids.")
@Version("0.0.1")
class ResourceIdMappingProviderResourcePatch : ResourcePatch() {
    companion object {
        internal lateinit var resourceMappings: Map<String, Long>
            private set
    }

    override fun execute(data: ResourceData): PatchResult {
        data.getXmlEditor("res/values/public.xml").use { editor ->
            resourceMappings = buildMap {
                editor.file.documentElement.doRecursively { node ->
                    if (node !is Element) return@doRecursively
                    val nameAttribute = node.getAttribute("name")
                    if (node.nodeName != "public" || nameAttribute.startsWith("APKTOOL")) return@doRecursively
                    this[nameAttribute] = node.getAttribute("id").substring(2).toLong(16)
                }
            }
        }

        return PatchResultSuccess()
    }
}