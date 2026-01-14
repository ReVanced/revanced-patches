package app.revanced.patches.reddit.ad.general

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.removeInstruction
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.reddit.ad.comments.`Hide comment ads`
import app.revanced.patches.reddit.misc.extension.sharedExtensionPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22c
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Suppress("unused", "ObjectPropertyName")
val `Hide ads` by creatingBytecodePatch {
    dependsOn(`Hide comment ads`, sharedExtensionPatch)

    compatibleWith("com.reddit.frontpage")

    apply {
        // region Filter promoted ads (does not work in popular or latest feed)

        val filterMethodDescriptor =
            "Lapp/revanced/extension/reddit/patches/FilterPromotedLinksPatch;" +
                    "->filterChildren(Ljava/lang/Iterable;)Ljava/util/List;"

        val setPostsListChildren = adPostMethod.implementation!!.instructions.first { instruction ->
            if (instruction.opcode != Opcode.IPUT_OBJECT) return@first false

            val reference = (instruction as ReferenceInstruction).reference as FieldReference
            reference.name == "children"
        }

        val castedInstruction = setPostsListChildren as Instruction22c
        val itemsRegister = castedInstruction.registerA
        val listInstanceRegister = castedInstruction.registerB

        // postsList.children = filterChildren(postListItems)
        adPostMethod.removeInstruction(setPostsListChildren.location.index)
        adPostMethod.addInstructions(
            setPostsListChildren.location.index,
            """
                invoke-static {v$itemsRegister}, $filterMethodDescriptor
                move-result-object v0
                iput-object v0, v$listInstanceRegister, ${castedInstruction.reference}
            """,
        )

        // endregion

        // region Remove ads from popular and latest feed

        // The new feeds work by inserting posts into lists.
        // AdElementConverter is conveniently responsible for inserting all feed ads.
        // By removing the appending instruction no ad posts gets appended to the feed.

        val index = newAdPostMethod.indexOfFirstInstruction {
            val reference = getReference<MethodReference>() ?: return@indexOfFirstInstruction false

            reference.name == "add" && reference.definingClass == "Ljava/util/ArrayList;"
        }

        newAdPostMethod.removeInstruction(index)
    }

    // endregion
}
