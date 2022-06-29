package app.revanced.patches.youtube.misc.mapping.patch

import app.revanced.extensions.doRecursively
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.ResourceData
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.impl.ResourcePatch
import org.w3c.dom.Element

@Name("resource-id-mapping-provider-resource-patch-dependency")
@Description("This patch acts as a provider/ dependency for resource mappings.")
@Version("0.0.1")
class ResourceIdMappingProviderResourcePatch : ResourcePatch() {
    companion object {
        internal lateinit var resourceMappings: List<ResourceElement>
            private set
    }

    override fun execute(data: ResourceData): PatchResult {
        data.xmlEditor["res/values/public.xml"].use { editor ->
            resourceMappings = buildList {
                editor.file.documentElement.doRecursively { node ->
                    if (node !is Element) return@doRecursively
                    val nameAttribute = node.getAttribute("name")
                    val typeAttribute = node.getAttribute("type")
                    if (node.nodeName != "public" || nameAttribute.startsWith("APKTOOL")) return@doRecursively
                    val id = node.getAttribute("id").substring(2).toLong(16)
                    add(ResourceElement(typeAttribute, nameAttribute, id))
                }
            }
        }

        return PatchResultSuccess()
    }
}

data class ResourceElement(val type: String, val name: String, val id: Long)
