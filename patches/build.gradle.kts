import org.w3c.dom.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

group = "app.revanced"

patches {
    about {
        name = "ReVanced Patches"
        description = "Patches for ReVanced"
        source = "git@github.com:revanced/revanced-patches.git"
        author = "ReVanced"
        contact = "contact@revanced.app"
        website = "https://revanced.app"
        license = "GNU General Public License v3.0"
    }
}

repositories {
    mavenLocal()
    gradlePluginPortal()
    google()
}

dependencies {
    // Required due to smali, or build fails. Can be removed once smali is bumped.
    implementation(libs.guava)

    implementation(libs.apksig)

    // Android API stubs defined here.
    compileOnly(project(":patches:stub"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexplicit-backing-fields",
            "-Xcontext-parameters"
        )
    }
}

publishing {
    repositories {
        maven {
            name = "githubPackages"
            url = uri("https://maven.pkg.github.com/revanced/revanced-patches")
            credentials(PasswordCredentials::class)
        }
    }
}

tasks.register("processStringsForCrowdin") {
    description = "Process strings file for Crowdin by commenting out non-standard tags."

    doLast {
        // Comment out the non-standard tags. Otherwise, Crowdin interprets the file
        // not as Android but instead a generic xml file where strings are
        // identified by xml position and not key
        val stringsXmlFile = project.projectDir.resolve("src/main/resources/addresources/values/strings.xml")

        val builder = DocumentBuilderFactory.newInstance().apply {
            isIgnoringComments = false
            isCoalescing = false
            isNamespaceAware = false
        }.newDocumentBuilder()

        val document = builder.newDocument()
        val root = document.createElement("resources").also(document::appendChild)

        fun walk(node: Node, appId: String? = null, patchId: String? = null, insideResources: Boolean = false) {
            fun walkChildren(el: Element, appId: String?, patchId: String?, insideResources: Boolean) {
                val children = el.childNodes
                for (i in 0 until children.length) {
                    walk(children.item(i), appId, patchId, insideResources)
                }
            }
            when (node.nodeType) {
                Node.COMMENT_NODE -> {
                    val comment = document.createComment(node.nodeValue)
                    if (insideResources) root.appendChild(comment) else document.insertBefore(comment, root)
                }

                Node.ELEMENT_NODE -> {
                    val element = node as Element

                    when (element.tagName) {
                        "resources" -> walkChildren(element, appId, patchId, insideResources = true)

                        "app" -> {
                            val newAppId = element.getAttribute("id")

                            root.appendChild(document.createComment(" <app id=\"$newAppId\"> "))
                            walkChildren(element, newAppId, patchId, insideResources)
                            root.appendChild(document.createComment(" </app> "))
                        }

                        "patch" -> {
                            val newPatchId = element.getAttribute("id")

                            root.appendChild(document.createComment(" <patch id=\"$newPatchId\"> "))
                            walkChildren(element, appId, newPatchId, insideResources)
                            root.appendChild(document.createComment(" </patch> "))
                        }

                        "string" -> {
                            val name = element.getAttribute("name")
                            val value = element.textContent
                            val fullName = "$appId.$patchId.$name"

                            val stringElement = document.createElement("string")
                            stringElement.setAttribute("name", fullName)
                            stringElement.appendChild(document.createTextNode(value))
                            root.appendChild(stringElement)
                        }

                        else -> walkChildren(element, appId, patchId, insideResources)
                    }
                }
            }
        }

        builder.parse(stringsXmlFile).let {
            val topLevel = it.childNodes
            for (i in 0 until topLevel.length) {
                val node = topLevel.item(i)
                if (node != it.documentElement) walk(node)
            }

            walk(it.documentElement)
        }

        TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.INDENT, "yes")
            setOutputProperty(OutputKeys.ENCODING, "utf-8")
        }.transform(DOMSource(document), StreamResult(stringsXmlFile))
    }
}
