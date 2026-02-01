import org.w3c.dom.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

fun parseXmlDocument(file: File): Pair<DocumentBuilder, Document> {
    val builder = DocumentBuilderFactory.newInstance().apply {
        isIgnoringComments = false
        isCoalescing = false
        isNamespaceAware = false
    }.newDocumentBuilder()

    return builder to builder.parse(file)
}

fun createXmlDocument(): Pair<DocumentBuilder, Document> {
    val builder = DocumentBuilderFactory.newInstance().apply {
        isIgnoringComments = false
        isCoalescing = false
        isNamespaceAware = false
    }.newDocumentBuilder()

    return builder to builder.newDocument()
}

fun writeXmlDocument(document: Document, file: File) {
    TransformerFactory.newInstance().newTransformer().apply {
        setOutputProperty(OutputKeys.INDENT, "yes")
        setOutputProperty(OutputKeys.ENCODING, "utf-8")
        setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
    }.transform(DOMSource(document), StreamResult(file))
}

fun NodeList.forEach(action: (Node) -> Unit) {
    for (i in 0 until length) {
        action(item(i))
    }
}

fun Element.getAppOrPatchId(): String? = if (tagName in setOf("app", "patch")) getAttribute("id") else null

fun buildFullName(appId: String?, patchId: String?, name: String): String = listOfNotNull(appId, patchId, name).joinToString(".")

fun buildPrefix(appId: String?, patchId: String?): String = listOfNotNull(appId, patchId).joinToString(".")

fun processStringElements(
    inputDoc: Document,
    outputDoc: Document,
    shouldCommentOut: Boolean = true,
    transform: (Element, String?, String?) -> Node?,
) {
    val root = outputDoc.createElement("resources").also(outputDoc::appendChild)

    fun walk(node: Node, appId: String? = null, patchId: String? = null, insideResources: Boolean = false, parentElement: Element = root) {
        when (node.nodeType) {
            Node.COMMENT_NODE -> {
                val comment = outputDoc.createComment(node.nodeValue)
                if (insideResources) parentElement.appendChild(comment) else outputDoc.insertBefore(comment, root)
            }

            Node.ELEMENT_NODE -> {
                val element = node as Element

                when (element.tagName) {
                    "resources" -> element.childNodes.forEach { walk(it, appId, patchId, true, parentElement) }

                    "app", "patch" -> {
                        val id = element.getAttribute("id")
                        val (newAppId, newPatchId) = if (element.tagName == "app") {
                            id to patchId
                        } else {
                            appId to id
                        }

                        if (shouldCommentOut) {
                            parentElement.appendChild(outputDoc.createComment(" <${element.tagName} id=\"$id\"> "))
                            element.childNodes.forEach { walk(it, newAppId, newPatchId, true, parentElement) }
                            parentElement.appendChild(outputDoc.createComment(" </${element.tagName}> "))
                        } else {
                            val newElement = outputDoc.createElement(element.tagName).apply {
                                setAttribute("id", id)
                            }
                            parentElement.appendChild(newElement)
                            element.childNodes.forEach { walk(it, newAppId, newPatchId, true, newElement) }
                        }
                    }

                    "string" -> transform(element, appId, patchId)?.let { parentElement.appendChild(it) }

                    else -> element.childNodes.forEach { walk(it, appId, patchId, true, parentElement) }
                }
            }
        }
    }

    inputDoc.childNodes.forEach {
        if (it != inputDoc.documentElement) walk(it)
    }
    walk(inputDoc.documentElement)
}

tasks.register("processStringsForCrowdin") {
    description = "Process strings file for Crowdin by flattening app/patch structure into string names."

    doLast {
        val stringsXmlFile = project.projectDir.resolve("src/main/resources/addresources/values/strings.xml")
        val (_, inputDoc) = parseXmlDocument(stringsXmlFile)
        val (_, outputDoc) = createXmlDocument()

        processStringElements(inputDoc, outputDoc, shouldCommentOut = true) { element, appId, patchId ->
            val name = element.getAttribute("name")
            val fullName = buildFullName(appId, patchId, name)

            outputDoc.createElement("string").apply {
                setAttribute("name", fullName)
                appendChild(outputDoc.createTextNode(element.textContent))
            }
        }

        writeXmlDocument(outputDoc, stringsXmlFile)
    }
}

tasks.register("processStringsFromCrowdin") {
    description = "Strip app/patch prefix from string names in localized files."

    doLast {
        val resDir = project.projectDir.resolve("src/main/resources/addresources")

        resDir.listFiles()?.filter {
            it.isDirectory && it.name.startsWith("values-")
        }?.forEach { valuesDir ->
            val stringsXmlFile = valuesDir.resolve("strings.xml")
            if (!stringsXmlFile.exists()) return@forEach

            val (_, inputDoc) = parseXmlDocument(stringsXmlFile)
            val (_, outputDoc) = createXmlDocument()

            processStringElements(inputDoc, outputDoc, shouldCommentOut = false) { element, appId, patchId ->
                val name = element.getAttribute("name")
                val prefix = buildPrefix(appId, patchId)
                val strippedName = if (prefix.isNotEmpty() && name.startsWith("$prefix.")) {
                    name.removePrefix("$prefix.")
                } else {
                    name
                }

                outputDoc.createElement("string").apply {
                    setAttribute("name", strippedName)
                    appendChild(outputDoc.createTextNode(element.textContent))
                }
            }

            writeXmlDocument(outputDoc, stringsXmlFile)
        }
    }
}
