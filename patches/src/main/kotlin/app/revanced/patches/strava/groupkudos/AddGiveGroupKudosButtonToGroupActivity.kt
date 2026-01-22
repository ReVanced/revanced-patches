package app.revanced.patches.strava.groupkudos

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.util.childElementsSequence
import app.revanced.util.findElementByAttributeValueOrThrow
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.AccessFlags.*
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction11x
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction31i
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import com.android.tools.smali.dexlib2.immutable.ImmutableClassDef
import com.android.tools.smali.dexlib2.immutable.ImmutableField
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import org.w3c.dom.Element

private const val VIEW_CLASS_DESCRIPTOR = "Landroid/view/View;"
private const val ON_CLICK_LISTENER_CLASS_DESCRIPTOR = "Landroid/view/View\$OnClickListener;"

private var shakeToKudosStringId = -1
private var kudosIdId = -1
private var leaveIdId = -1

private val addGiveKudosButtonToLayoutPatch = resourcePatch {
    fun String.toResourceId() = substring(2).toInt(16)

    execute {
        document("res/values/public.xml").use { public ->
            fun Sequence<Element>.firstByName(name: String) = first {
                it.getAttribute("name") == name
            }

            val publicElements = public.documentElement.childElementsSequence().filter {
                it.tagName == "public"
            }
            val idElements = publicElements.filter {
                it.getAttribute("type") == "id"
            }
            val stringElements = publicElements.filter {
                it.getAttribute("type") == "string"
            }

            shakeToKudosStringId =
                stringElements.firstByName("shake_to_kudos_dialog_title").getAttribute("id").toResourceId()

            val kudosIdNode = idElements.firstByName("kudos").apply {
                kudosIdId = getAttribute("id").toResourceId()
            }

            document("res/layout/grouped_activities_dialog_group_tab.xml").use { layout ->
                layout.childNodes.findElementByAttributeValueOrThrow("android:id", "@id/leave_group_button_container")
                    .apply {
                        // Change from "FrameLayout".
                        layout.renameNode(this, namespaceURI, "LinearLayout")

                        val leaveButton = childElementsSequence().first()
                        // Get "Leave Group" button ID for bytecode matching.
                        val leaveButtonIdName = leaveButton.getAttribute("android:id").substringAfter('/')
                        leaveIdId = idElements.firstByName(leaveButtonIdName).getAttribute("id").toResourceId()

                        // Add surrounding padding to offset decrease on buttons.
                        setAttribute("android:paddingHorizontal", "@dimen/space_2xs")

                        // Place buttons next to each other with equal width.
                        val kudosButton = leaveButton.apply {
                            setAttribute("android:layout_width", "0dp")
                            setAttribute("android:layout_weight", "1")
                            // Decrease padding between buttons from "@dimen/button_large_padding" ...
                            setAttribute("android:paddingHorizontal", "@dimen/space_xs")
                        }.cloneNode(true) as Element
                        kudosButton.apply {
                            setAttribute("android:id", "@id/${kudosIdNode.getAttribute("name")}")
                            setAttribute("android:text", "@string/kudos_button")
                        }.let(::appendChild)

                        // Downgrade emphasis of "Leave Group" button from "primary".
                        leaveButton.setAttribute("app:emphasis", "secondary")
                    }
            }
        }
    }
}

@Suppress("unused")
val addGiveGroupKudosButtonToGroupActivity = bytecodePatch(
    name = "Add 'Give Kudos' button to 'Group Activity'",
    description = "Adds a button that triggers the same action as shaking your phone would."
) {
    compatibleWith("com.strava")

    dependsOn(addGiveKudosButtonToLayoutPatch)

    execute {
        val className = initFingerprint.originalClassDef.type
        val onClickListenerClassName = "${className.substringBeforeLast(';')}\$GiveKudosOnClickListener;"

        initFingerprint.method.apply {
            val constLeaveIdInstruction = instructions.filterIsInstance<BuilderInstruction31i>().first {
                it.narrowLiteral == leaveIdId
            }
            val findViewByIdInstruction =
                getInstruction<BuilderInstruction35c>(constLeaveIdInstruction.location.index + 1)
            val moveViewInstruction = getInstruction<BuilderInstruction11x>(constLeaveIdInstruction.location.index + 2)
            val checkCastButtonInstruction =
                getInstruction<BuilderInstruction21c>(constLeaveIdInstruction.location.index + 3)

            val buttonClassName = checkCastButtonInstruction.getReference<TypeReference>()!!.type

            addInstructions(
                constLeaveIdInstruction.location.index,
                """
                    ${constLeaveIdInstruction.opcode.name} v${constLeaveIdInstruction.registerA}, $kudosIdId
                    ${findViewByIdInstruction.opcode.name} { v${findViewByIdInstruction.registerC}, v${findViewByIdInstruction.registerD} }, ${findViewByIdInstruction.reference}
                    ${moveViewInstruction.opcode.name} v${moveViewInstruction.registerA}
                    ${checkCastButtonInstruction.opcode.name} v${checkCastButtonInstruction.registerA}, ${checkCastButtonInstruction.reference}
                    new-instance v0, $onClickListenerClassName
                    invoke-direct { v0, p0 }, $onClickListenerClassName-><init>($className)V
                    invoke-virtual { p3, v0 }, $buttonClassName->setOnClickListener($ON_CLICK_LISTENER_CLASS_DESCRIPTOR)V
                """
            )
        }

        val actionHandlerMethod = actionHandlerFingerprint.match(initFingerprint.originalClassDef).method
        val constShakeToKudosStringIndex = actionHandlerMethod.instructions.indexOfFirst {
            it is NarrowLiteralInstruction && it.narrowLiteral == shakeToKudosStringId
        }
        val getSingletonInstruction = actionHandlerMethod.instructions.filterIsInstance<BuilderInstruction21c>().last {
            it.opcode == Opcode.SGET_OBJECT && it.location.index < constShakeToKudosStringIndex
        }

        val outerThisField = ImmutableField(
            onClickListenerClassName,
            "outerThis",
            className,
            PUBLIC.value or FINAL.value or SYNTHETIC.value,
            null,
            listOf(),
            setOf()
        )

        val initFieldMethod = ImmutableMethod(
            onClickListenerClassName,
            "<init>",
            listOf(ImmutableMethodParameter(className, setOf(), "outerThis")),
            "V",
            PUBLIC.value or SYNTHETIC.value or CONSTRUCTOR.value,
            setOf(),
            setOf(),
            MutableMethodImplementation(2)
        ).toMutable().apply {
            addInstructions(
                """
                    invoke-direct {p0}, Ljava/lang/Object;-><init>()V
                    iput-object p1, p0, $outerThisField
                    return-void
                """
            )
        }

        val onClickMethod = ImmutableMethod(
            onClickListenerClassName,
            "onClick",
            listOf(ImmutableMethodParameter(VIEW_CLASS_DESCRIPTOR, setOf(), "v")),
            "V",
            PUBLIC.value or FINAL.value,
            setOf(),
            setOf(),
            MutableMethodImplementation(2)
        ).toMutable().apply {
            addInstructions(
                """
                    sget-object p1, ${getSingletonInstruction.reference}
                    iget-object p0, p0, $outerThisField
                    invoke-virtual { p0, p1 }, ${actionHandlerFingerprint.method}
                    return-void
                """
            )
        }

        ImmutableClassDef(
            onClickListenerClassName,
            PUBLIC.value or FINAL.value or SYNTHETIC.value,
            "Ljava/lang/Object;",
            listOf(ON_CLICK_LISTENER_CLASS_DESCRIPTOR),
            "ProGuard", // Same as source file name of other classes.
            listOf(),
            setOf(outerThisField),
            setOf(initFieldMethod, onClickMethod)
        ).let(classes::add)
    }
}
