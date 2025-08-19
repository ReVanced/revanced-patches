package app.revanced.patches.youtube.misc.playercontrols

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.Document
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_25_or_greater
import app.revanced.patches.youtube.misc.playservice.is_19_35_or_greater
import app.revanced.util.*
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import org.w3c.dom.Node

/**
 * Add a new top to the bottom of the YouTube player.
 *
 * @param resourceDirectoryName The name of the directory containing the hosting resource.
 */
@Suppress("KDocUnresolvedReference")
// Internal until this is modified to work with any patch (and not just SponsorBlock).
internal lateinit var addTopControl: (String) -> Unit
    private set

/**
 * Add a new bottom to the bottom of the YouTube player.
 *
 * @param resourceDirectoryName The name of the directory containing the hosting resource.
 */
@Suppress("KDocUnresolvedReference")
lateinit var addBottomControl: (String) -> Unit
    private set

internal var bottom_ui_container_stub_id = -1L
    private set
internal var controls_layout_stub_id = -1L
    private set
internal var heatseeker_viewstub_id = -1L
    private set
internal var fullscreen_button_id = -1L
    private set
internal var inset_overlay_view_layout_id = -1L
    private set
internal var scrim_overlay_id = -1L
    private set

val playerControlsResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    /**
     * The element to the left of the element being added.
     */
    /**
     * The element to the left of the element being added.
     */
    var bottomLastLeftOf = "@id/fullscreen_button"

    lateinit var bottomTargetDocument: Document

    execute {
        val targetResourceName = "youtube_controls_bottom_ui_container.xml"

        bottom_ui_container_stub_id = resourceMappings["id", "bottom_ui_container_stub"]
        controls_layout_stub_id = resourceMappings["id", "controls_layout_stub"]
        heatseeker_viewstub_id = resourceMappings["id", "heatseeker_viewstub"]
        fullscreen_button_id = resourceMappings["id", "fullscreen_button"]
        inset_overlay_view_layout_id = resourceMappings["id", "inset_overlay_view_layout"]
        scrim_overlay_id = resourceMappings["id", "scrim_overlay"]

        bottomTargetDocument = document("res/layout/$targetResourceName")

        val bottomTargetElement: Node = bottomTargetDocument.getElementsByTagName(
            "android.support.constraint.ConstraintLayout",
        ).item(0)

        val bottomTargetDocumentChildNodes = bottomTargetDocument.childNodes
        var bottomInsertBeforeNode: Node = bottomTargetDocumentChildNodes.findElementByAttributeValueOrThrow(
            "android:inflatedId",
            bottomLastLeftOf,
        )

        addTopControl = { resourceDirectoryName ->
            val resourceFileName = "host/layout/youtube_controls_layout.xml"
            val hostingResourceStream = inputStreamFromBundledResource(
                resourceDirectoryName,
                resourceFileName,
            ) ?: throw PatchException("Could not find $resourceFileName")

            val document = document("res/layout/youtube_controls_layout.xml")

            "RelativeLayout".copyXmlNode(
                document(hostingResourceStream),
                document,
            ).use {
                val element = document.childNodes.findElementByAttributeValueOrThrow(
                    "android:id",
                    "@id/player_video_heading",
                )

                // FIXME: This uses hard coded values that only works with SponsorBlock.
                // If other top buttons are added by other patches, this code must be changed.
                // voting button id from the voting button view from the youtube_controls_layout.xml host file
                val votingButtonId = "@+id/revanced_sb_voting_button"
                element.attributes.getNamedItem("android:layout_toStartOf").nodeValue = votingButtonId
            }
        }

        addBottomControl = { resourceDirectoryName ->
            val resourceFileName = "host/layout/youtube_controls_bottom_ui_container.xml"
            val sourceDocument = document(
                inputStreamFromBundledResource(resourceDirectoryName, resourceFileName)
                    ?: throw PatchException("Could not find $resourceFileName"),
            )

            val sourceElements = sourceDocument.getElementsByTagName(
                "android.support.constraint.ConstraintLayout",
            ).item(0).childNodes

            // Copy the patch layout xml into the target layout file.
            for (index in sourceElements.length - 1 downTo 1) {
                val element = sourceElements.item(index).cloneNode(true)

                // If the element has no attributes there's no point adding it to the destination.
                if (!element.hasAttributes()) continue

                element.attributes.getNamedItem("yt:layout_constraintRight_toLeftOf").nodeValue = bottomLastLeftOf
                bottomLastLeftOf = element.attributes.getNamedItem("android:id").nodeValue

                bottomTargetDocument.adoptNode(element)
                // Elements do not need to be added in the layout order since a layout constraint is used,
                // but in order is easier to make sense of while debugging.
                bottomTargetElement.insertBefore(element, bottomInsertBeforeNode)
                bottomInsertBeforeNode = element
            }

            sourceDocument.close()
        }
    }

    finalize {
        val childNodes = bottomTargetDocument.childNodes

        arrayOf(
            "@id/bottom_end_container",
            "@id/multiview_button",
        ).forEach {
            childNodes.findElementByAttributeValue(
                "android:id",
                it,
            )?.setAttribute("yt:layout_constraintRight_toLeftOf", bottomLastLeftOf)
        }

        bottomTargetDocument.close()
    }
}

/**
 * Injects the code to initialize the controls.
 * @param descriptor The descriptor of the method which should be called.
 */
internal fun initializeTopControl(descriptor: String) {
    inflateTopControlMethod.addInstruction(
        inflateTopControlInsertIndex++,
        "invoke-static { v$inflateTopControlRegister }, $descriptor->initialize(Landroid/view/View;)V",
    )
}

/**
 * Injects the code to initialize the controls.
 * @param descriptor The descriptor of the method which should be called.
 */
fun initializeBottomControl(descriptor: String) {
    inflateBottomControlMethod.addInstruction(
        inflateBottomControlInsertIndex++,
        "invoke-static { v$inflateBottomControlRegister }, $descriptor->initializeButton(Landroid/view/View;)V",
    )
}

/**
 * Injects the code to change the visibility of controls.
 * @param descriptor The descriptor of the method which should be called.
 */
fun injectVisibilityCheckCall(descriptor: String) {
    visibilityMethod.addInstruction(
        visibilityInsertIndex++,
        "invoke-static { p1 , p2 }, $descriptor->setVisibility(ZZ)V",
    )

    if (!visibilityImmediateCallbacksExistModified) {
        visibilityImmediateCallbacksExistModified = true
        visibilityImmediateCallbacksExistMethod.returnEarly(true)
    }

    visibilityImmediateMethod.addInstruction(
        visibilityImmediateInsertIndex++,
        "invoke-static { p0 }, $descriptor->setVisibilityImmediate(Z)V",
    )

    // Patch works without this hook, but it is needed to use the correct fade out animation
    // duration when tapping the overlay to dismiss.
    visibilityNegatedImmediateMethod.addInstruction(
        visibilityNegatedImmediateInsertIndex++,
        "invoke-static { }, $descriptor->setVisibilityNegatedImmediate()V",
    )
}

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/PlayerControlsPatch;"

private lateinit var inflateTopControlMethod: MutableMethod
private var inflateTopControlInsertIndex: Int = -1
private var inflateTopControlRegister: Int = -1

private lateinit var inflateBottomControlMethod: MutableMethod
private var inflateBottomControlInsertIndex: Int = -1
private var inflateBottomControlRegister: Int = -1

private lateinit var visibilityMethod: MutableMethod
private var visibilityInsertIndex: Int = 0

private var visibilityImmediateCallbacksExistModified = false
private lateinit var visibilityImmediateCallbacksExistMethod : MutableMethod

private lateinit var visibilityImmediateMethod: MutableMethod
private var visibilityImmediateInsertIndex: Int = 0

private lateinit var visibilityNegatedImmediateMethod: MutableMethod
private var visibilityNegatedImmediateInsertIndex: Int = 0

val playerControlsPatch = bytecodePatch(
    description = "Manages the code for the player controls of the YouTube player.",
) {
    dependsOn(
        playerControlsResourcePatch,
        sharedExtensionPatch,
        PlayerControlsOverlayVisibilityPatch
    )

    execute {
        fun MutableMethod.indexOfFirstViewInflateOrThrow() = indexOfFirstInstructionOrThrow {
            val reference = getReference<MethodReference>()
            reference?.definingClass == "Landroid/view/ViewStub;" &&
                reference.name == "inflate"
        }

        playerBottomControlsInflateFingerprint.method.apply {
            inflateBottomControlMethod = this

            val inflateReturnObjectIndex = indexOfFirstViewInflateOrThrow() + 1
            inflateBottomControlRegister = getInstruction<OneRegisterInstruction>(inflateReturnObjectIndex).registerA
            inflateBottomControlInsertIndex = inflateReturnObjectIndex + 1
        }

        playerTopControlsInflateFingerprint.method.apply {
            inflateTopControlMethod = this

            val inflateReturnObjectIndex = indexOfFirstViewInflateOrThrow() + 1
            inflateTopControlRegister = getInstruction<OneRegisterInstruction>(inflateReturnObjectIndex).registerA
            inflateTopControlInsertIndex = inflateReturnObjectIndex + 1
        }

        visibilityMethod = controlsOverlayVisibilityFingerprint.match(
            playerTopControlsInflateFingerprint.originalClassDef,
        ).method

        // Hook the fullscreen close button.  Used to fix visibility
        // when seeking and other situations.
        overlayViewInflateFingerprint.method.apply {
            val resourceIndex = indexOfFirstLiteralInstructionReversedOrThrow(fullscreen_button_id)

            val index = indexOfFirstInstructionOrThrow(resourceIndex) {
                opcode == Opcode.CHECK_CAST &&
                    getReference<TypeReference>()?.type ==
                    "Landroid/widget/ImageView;"
            }
            val register = getInstruction<OneRegisterInstruction>(index).registerA

            addInstruction(
                index + 1,
                "invoke-static { v$register }, " +
                    "$EXTENSION_CLASS_DESCRIPTOR->setFullscreenCloseButton(Landroid/widget/ImageView;)V",
            )
        }

        visibilityImmediateCallbacksExistMethod = playerControlsExtensionHookListenersExistFingerprint.method
        visibilityImmediateMethod = playerControlsExtensionHookFingerprint.method

        motionEventFingerprint.match(youtubeControlsOverlayFingerprint.originalClassDef).method.apply {
            visibilityNegatedImmediateMethod = this
            visibilityNegatedImmediateInsertIndex = indexOfTranslationInstruction(this) + 1
        }

        // A/B test for a slightly different bottom overlay controls,
        // that uses layout file youtube_video_exploder_controls_bottom_ui_container.xml
        // The change to support this is simple and only requires adding buttons to both layout files,
        // but for now force this different layout off since it's still an experimental test.
        if (is_19_35_or_greater) {
            playerBottomControlsExploderFeatureFlagFingerprint.method.returnLate(false)
        }

        // A/B test of new top overlay controls. Two different layouts can be used:
        // youtube_cf_navigation_improvement_controls_layout.xml
        // youtube_cf_minimal_impact_controls_layout.xml
        //
        // Visually there is no noticeable difference between either of these compared to the default.
        // There is additional logic that is active when youtube_cf_navigation_improvement_controls_layout
        // is active, but what it does is not entirely clear.
        //
        // For now force this a/b feature off as it breaks the top player buttons.
        if (is_19_25_or_greater) {
            playerTopControlsExperimentalLayoutFeatureFlagFingerprint.method.apply {
                val index = indexOfFirstInstructionOrThrow(Opcode.MOVE_RESULT_OBJECT)
                val register = getInstruction<OneRegisterInstruction>(index).registerA

                addInstruction(
                    index + 1,
                    "const-string v$register, \"default\""
                )
            }
        }
    }
}
