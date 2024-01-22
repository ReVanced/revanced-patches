package app.revanced.patches.shared.misc.settings.preference.impl

import app.revanced.patches.shared.misc.settings.preference.BasePreference
import app.revanced.util.resource.BaseResource
import org.w3c.dom.Document

/**
 * A text preference.
 */
@Suppress("MemberVisibilityCanBePrivate")
class TextPreference : BasePreference {
    val inputType: InputType

    constructor(
        key: String? = null,
        titleKey: String,
        summaryKey: String?,
        inputType: InputType = InputType.TEXT,
        tag: String = "app.revanced.integrations.shared.settings.preference.ResettableEditTextPreference"
    ) : super(
        key,
        titleKey,
        summaryKey,
        tag
    ) {
        this.inputType = inputType
    }

    constructor(
        key: String,
        inputType: InputType = InputType.TEXT,
        tag: String = "app.revanced.integrations.shared.settings.preference.ResettableEditTextPreference"
    ) : super(key, tag) {
        this.inputType = inputType
    }

    override fun serialize(ownerDocument: Document, resourceCallback: (BaseResource) -> Unit) =
        super.serialize(ownerDocument, resourceCallback).apply {
            setAttribute("android:inputType", inputType.type)
        }
}
