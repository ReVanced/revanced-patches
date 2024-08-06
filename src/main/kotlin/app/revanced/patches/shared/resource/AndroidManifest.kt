package app.revanced.patches.shared.resource

import app.revanced.patcher.data.ResourceContext
import app.revanced.util.asSequence
import org.w3c.dom.Element

object AndroidManifest {

    private const val META_DATA_TAG = "meta-data"
    private const val NAME_ATTRIBUTE = "android:name"
    private const val VALUE_ATTRIBUTE = "android:value"

    fun addMetadata(context: ResourceContext, nodeName: String, nodeValue: String){
        context.document["AndroidManifest.xml"].use { document ->
            val applicationNode = document.getElementsByTagName("application").item(0) as Element

            // Try to find an existing node
            val metaDataTag = applicationNode.getElementsByTagName(META_DATA_TAG)
                .asSequence()
                .firstOrNull { it.attributes.getNamedItem(NAME_ATTRIBUTE).nodeValue == nodeName }

            // If it exists change its value
            if (metaDataTag != null) {
                metaDataTag.attributes.getNamedItem(VALUE_ATTRIBUTE).nodeValue = nodeValue
            }else{
                // Otherwise create a new node
                applicationNode.appendChild(
                    document.createElement(META_DATA_TAG).also { metaDataNode ->
                        metaDataNode.setAttribute(NAME_ATTRIBUTE, nodeName)
                        metaDataNode.setAttribute(VALUE_ATTRIBUTE, nodeValue)
                    }
                )
            }
        }
    }
}