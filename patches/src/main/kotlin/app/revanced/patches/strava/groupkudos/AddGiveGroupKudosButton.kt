package app.revanced.patches.strava.groupkudos

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.util.childElementsSequence
import app.revanced.util.findElementByAttributeValueOrThrow
import com.android.tools.smali.dexlib2.AccessFlags.*
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction11x
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction31i
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import com.android.tools.smali.dexlib2.immutable.ImmutableClassDef
import com.android.tools.smali.dexlib2.immutable.ImmutableField
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import com.google.common.collect.ImmutableSortedSet
import org.w3c.dom.Element
import kotlin.properties.Delegates

private const val VIEW_CLASS_DESCRIPTOR = "Landroid/view/View;"
private const val ON_CLICK_LISTENER_CLASS_DESCRIPTOR = "Landroid/view/View\$OnClickListener;"

private var shakeToKudosString by Delegates.notNull<Int>()
private var kudosId by Delegates.notNull<Int>()
private var leaveId by Delegates.notNull<Int>()

private val addGiveKudosButtonToLayoutPatch = resourcePatch {
    fun String.toResourceId() = substring(2).toInt(16)

    execute {
        document("res/values/public.xml").use { public ->
            public.documentElement.childElementsSequence().first { child ->
                child.tagName == "public" &&
                        child.getAttribute("type") == "string" &&
                        child.getAttribute("name").startsWith("shake_to_kudos")
            }.apply {
                shakeToKudosString = getAttribute("id").toResourceId()
            }
            val kudosIdNode = public.documentElement.childElementsSequence().first { child ->
                child.tagName == "public" &&
                        child.getAttribute("type") == "id" &&
                        child.getAttribute("name").startsWith("kudos")
            }.apply {
                kudosId = getAttribute("id").toResourceId()
            }

            document("res/layout/grouped_activities_dialog_group_tab.xml").use { layout ->
                layout.childNodes.findElementByAttributeValueOrThrow("android:id", "@id/leave_group_button_container")
                    .apply {
                        // Change from "FrameLayout".
                        layout.renameNode(this, namespaceURI, "LinearLayout")
                        val leaveButton = childElementsSequence().first()
                        // Get "Leave Group" button ID for bytecode matching.
                        val leaveButtonIdName = leaveButton.getAttribute("android:id").substringAfter('/')
                        public.documentElement.childElementsSequence().first { child ->
                            child.tagName == "public" &&
                                    child.getAttribute("type") == "id" &&
                                    child.getAttribute("name") == leaveButtonIdName
                        }.apply {
                            leaveId = getAttribute("id").toResourceId()
                        }
                        // Place buttons next to each other with equal width.
                        leaveButton.setAttribute("android:layout_width", "0dp")
                        leaveButton.setAttribute("android:layout_weight", "1")
                        // Decrease padding between buttons from "@dimen/button_large_padding" ...
                        leaveButton.setAttribute("android:paddingHorizontal", "@dimen/space_xs")
                        // ... while keeping the surrounding padding by adding to the container.
                        setAttribute("android:paddingHorizontal", "@dimen/space_2xs")
                        val kudosButton = leaveButton.cloneNode(true) as Element
                        // Downgrade emphasis of "Leave Group" button from "primary".
                        leaveButton.setAttribute("app:emphasis", "secondary")
                        kudosButton.setAttribute("android:id", "@id/${kudosIdNode.getAttribute("name")}")
                        kudosButton.setAttribute("android:text", "@string/kudos_button")
                        appendChild(kudosButton)
                    }
            }
        }
    }
}

@Suppress("unused")
val addGiveGroupKudosButton = bytecodePatch(
    name = "Add Give Kudos button to Group Activity",
    description = "The button triggers the same action as shaking your phone would."
) {
    compatibleWith("com.strava")

    dependsOn(addGiveKudosButtonToLayoutPatch)

    execute {
        val className = initFingerprint.originalClassDef.type
        val onClickListenerClassName = "${className.substringBeforeLast(';')}\$GiveKudosOnClickListener;"

        initFingerprint.method.apply {
            val constLeaveId = instructions.filterIsInstance<BuilderInstruction31i>().first {
                it.narrowLiteral == leaveId
            }
            val findViewById = getInstruction<BuilderInstruction35c>(constLeaveId.location.index + 1)
            val moveView = getInstruction<BuilderInstruction11x>(constLeaveId.location.index + 2)
            val castButton = getInstruction<BuilderInstruction21c>(constLeaveId.location.index + 3)
            val buttonClassName = (castButton.reference as TypeReference).type

            addInstructions(
                constLeaveId.location.index,
                """
                    ${constLeaveId.opcode.name} v${constLeaveId.registerA}, $kudosId
                    ${findViewById.opcode.name} { v${findViewById.registerC}, v${findViewById.registerD} }, ${findViewById.reference}
                    ${moveView.opcode.name} v${moveView.registerA}
                    ${castButton.opcode.name} v${castButton.registerA}, ${castButton.reference}
                    new-instance v0, $onClickListenerClassName
                    invoke-direct { v0, p0 }, $onClickListenerClassName-><init>($className)V
                    invoke-virtual { p3, v0 }, $buttonClassName->setOnClickListener($ON_CLICK_LISTENER_CLASS_DESCRIPTOR)V
                """
            )
        }

        actionHandlerFingerprint.match(initFingerprint.originalClassDef).method.apply {
            val constShakeToKudosString = instructions.filterIsInstance<BuilderInstruction31i>().first {
                it.narrowLiteral == shakeToKudosString
            }
            val getSingleton = instructions.filterIsInstance<BuilderInstruction21c>().last {
                it.opcode == Opcode.SGET_OBJECT && it.location.index < constShakeToKudosString.location.index
            }
            val outerThis = ImmutableField(
                onClickListenerClassName,
                "outerThis",
                className,
                PUBLIC.value or FINAL.value or SYNTHETIC.value,
                null,
                listOf(),
                setOf()
            )
            val init = ImmutableMethod(
                onClickListenerClassName,
                "<init>",
                listOf(
                    ImmutableMethodParameter(
                        className,
                        setOf(),
                        "outerThis"
                    ),
                ),
                "V",
                PUBLIC.value or SYNTHETIC.value or CONSTRUCTOR.value,
                setOf(),
                setOf(),
                MutableMethodImplementation(2)
            ).toMutable().apply {
                addInstructions(
                    """
                        invoke-direct {p0}, Ljava/lang/Object;-><init>()V
                        iput-object p1, p0, $outerThis
                        return-void
                    """
                )
            }
            val onClick = ImmutableMethod(
                onClickListenerClassName,
                "onClick",
                listOf(
                    ImmutableMethodParameter(
                        VIEW_CLASS_DESCRIPTOR,
                        setOf(),
                        "v"
                    ),
                ),
                "V",
                PUBLIC.value or FINAL.value,
                setOf(),
                setOf(),
                MutableMethodImplementation(2)
            ).toMutable().apply {
                addInstructions(
                    """
                        sget-object p1, ${getSingleton.reference}
                        iget-object p0, p0, $outerThis
                        invoke-virtual { p0, p1 }, ${actionHandlerFingerprint.method}
                        return-void
                    """
                )
            }
            val onClickListener = ImmutableClassDef(
                onClickListenerClassName,
                PUBLIC.value or FINAL.value or SYNTHETIC.value,
                "Ljava/lang/Object;",
                listOf(ON_CLICK_LISTENER_CLASS_DESCRIPTOR),
                "ProGuard",
                listOf(),
                ImmutableSortedSet.of(outerThis),
                ImmutableSortedSet.of(init, onClick)
            )

            classes.add(onClickListener)
        }
    }
}
