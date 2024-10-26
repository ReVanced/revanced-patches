package app.revanced.patches.twitch.misc.settings

import app.revanced.patcher.Match
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableField.Companion.toMutable
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.BasePreference
import app.revanced.patches.shared.misc.settings.preference.BasePreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.PreferenceCategory
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.settingsPatch
import app.revanced.patches.twitch.misc.extension.sharedExtensionPatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.immutable.ImmutableField

private const val REVANCED_SETTINGS_MENU_ITEM_NAME = "RevancedSettings"
private const val REVANCED_SETTINGS_MENU_ITEM_ID = 0x7
private const val REVANCED_SETTINGS_MENU_ITEM_TITLE_RES = "revanced_settings"
private const val REVANCED_SETTINGS_MENU_ITEM_ICON_RES = "ic_settings"

private const val MENU_ITEM_ENUM_CLASS_DESCRIPTOR = "Ltv/twitch/android/feature/settings/menu/SettingsMenuItem;"
private const val MENU_DISMISS_EVENT_CLASS_DESCRIPTOR =
    "Ltv/twitch/android/feature/settings/menu/SettingsMenuViewDelegate\$Event\$OnDismissClicked;"

private const val EXTENSION_PACKAGE = "app/revanced/extension/twitch"
private const val ACTIVITY_HOOKS_CLASS_DESCRIPTOR = "L$EXTENSION_PACKAGE/settings/AppCompatActivityHook;"
private const val UTILS_CLASS_DESCRIPTOR = "L$EXTENSION_PACKAGE/Utils;"

private val preferences = mutableSetOf<BasePreference>()

fun addSettingPreference(screen: BasePreference) {
    preferences += screen
}

val settingsPatch = bytecodePatch(
    name = "Settings",
    description = "Adds settings menu to Twitch.",
) {
    dependsOn(
        sharedExtensionPatch,
        addResourcesPatch,
        settingsPatch(preferences = preferences),
    )

    compatibleWith(
        "tv.twitch.android.app"(
            "15.4.1",
            "16.1.0",
            "16.9.1",
        ),
    )

    val settingsActivityOnCreateMatch by settingsActivityOnCreateFingerprint()
    val settingsMenuItemEnumMatch by settingsMenuItemEnumFingerprint()
    val menuGroupsUpdatedMatch by menuGroupsUpdatedFingerprint()
    val menuGroupsOnClickMatch by menuGroupsOnClickFingerprint()

    execute {
        addResources("twitch", "misc.settings.settingsPatch")

        PreferenceScreen.MISC.OTHER.addPreferences(
            // The debug setting is shared across multiple apps and the key must be the same.
            // But the title and summary must be different, otherwise when the strings file is flattened
            // for Crowdin push, Crowdin gets confused by the duplicate keys.
            // FIXME: Ideally the shared debug strings are extracted into a common app group
            //  and then both apps import that. But for now unique unique title and summary keys also works.
            SwitchPreference(
                key = "revanced_debug",
                titleKey = "revanced_twitch_debug_title",
                summaryOnKey = "revanced_twitch_debug_summary_on",
                summaryOffKey = "revanced_twitch_debug_summary_off",
            ),
        )

        // Hook onCreate to handle fragment creation.
        val insertIndex = settingsActivityOnCreateMatch.mutableMethod.implementation!!.instructions.size - 2
        settingsActivityOnCreateMatch.mutableMethod.addInstructionsWithLabels(
            insertIndex,
            """
                invoke-static { p0 }, $ACTIVITY_HOOKS_CLASS_DESCRIPTOR->handleSettingsCreation(Landroidx/appcompat/app/AppCompatActivity;)Z
                move-result v0
                if-eqz v0, :no_rv_settings_init
                return-void
            """,
            ExternalLabel(
                "no_rv_settings_init",
                settingsActivityOnCreateMatch.mutableMethod.getInstruction(insertIndex),
            ),
        )

        // Create new menu item for settings menu.
        fun Match.injectMenuItem(
            name: String,
            value: Int,
            titleResourceName: String,
            iconResourceName: String,
        ) {
            // Add new static enum member field
            mutableClass.staticFields.add(
                ImmutableField(
                    mutableMethod.definingClass,
                    name,
                    MENU_ITEM_ENUM_CLASS_DESCRIPTOR,
                    AccessFlags.PUBLIC.value or
                        AccessFlags.FINAL.value or
                        AccessFlags.ENUM.value or
                        AccessFlags.STATIC.value,
                    null,
                    null,
                    null,
                ).toMutable(),
            )

            // Add initializer for the new enum member
            mutableMethod.addInstructions(
                mutableMethod.implementation!!.instructions.size - 4,
                """   
                new-instance        v0, $MENU_ITEM_ENUM_CLASS_DESCRIPTOR
                const-string        v1, "$titleResourceName"
                invoke-static       {v1}, $UTILS_CLASS_DESCRIPTOR->getStringId(Ljava/lang/String;)I
                move-result         v1
                const-string        v3, "$iconResourceName"
                invoke-static       {v3}, $UTILS_CLASS_DESCRIPTOR->getDrawableId(Ljava/lang/String;)I
                move-result         v3
                const-string        v4, "$name"
                const/4             v5, $value
                invoke-direct       {v0, v4, v5, v1, v3}, $MENU_ITEM_ENUM_CLASS_DESCRIPTOR-><init>(Ljava/lang/String;III)V 
                sput-object         v0, $MENU_ITEM_ENUM_CLASS_DESCRIPTOR->$name:$MENU_ITEM_ENUM_CLASS_DESCRIPTOR
            """,
            )
        }

        settingsMenuItemEnumMatch.injectMenuItem(
            REVANCED_SETTINGS_MENU_ITEM_NAME,
            REVANCED_SETTINGS_MENU_ITEM_ID,
            REVANCED_SETTINGS_MENU_ITEM_TITLE_RES,
            REVANCED_SETTINGS_MENU_ITEM_ICON_RES,
        )

        // Intercept settings menu creation and add new menu item.
        menuGroupsUpdatedMatch.mutableMethod.addInstructions(
            0,
            """
                sget-object v0, $MENU_ITEM_ENUM_CLASS_DESCRIPTOR->$REVANCED_SETTINGS_MENU_ITEM_NAME:$MENU_ITEM_ENUM_CLASS_DESCRIPTOR 
                invoke-static { p1, v0 }, $ACTIVITY_HOOKS_CLASS_DESCRIPTOR->handleSettingMenuCreation(Ljava/util/List;Ljava/lang/Object;)Ljava/util/List;
                move-result-object p1
            """,
        )

        // Intercept onclick events for the settings menu

        menuGroupsOnClickMatch.mutableMethod.addInstructionsWithLabels(
            0,
            """
                invoke-static {p1}, $ACTIVITY_HOOKS_CLASS_DESCRIPTOR->handleSettingMenuOnClick(Ljava/lang/Enum;)Z
                move-result p2
                if-eqz p2, :no_rv_settings_onclick
                sget-object p1, $MENU_DISMISS_EVENT_CLASS_DESCRIPTOR->INSTANCE:$MENU_DISMISS_EVENT_CLASS_DESCRIPTOR
                invoke-virtual { p0, p1 }, Ltv/twitch/android/core/mvp/viewdelegate/RxViewDelegate;->pushEvent(Ltv/twitch/android/core/mvp/viewdelegate/ViewDelegateEvent;)V
                return-void
            """,
            ExternalLabel(
                "no_rv_settings_onclick",
                menuGroupsOnClickMatch.mutableMethod.getInstruction(0),
            ),
        )
    }

    finalize {
        PreferenceScreen.close()
    }
}

/**
 * Preference screens patches should add their settings to.
 */
@Suppress("ktlint:standard:property-naming")
internal object PreferenceScreen : BasePreferenceScreen() {
    val ADS = CustomScreen("revanced_ads_screen")
    val CHAT = CustomScreen("revanced_chat_screen")
    val MISC = CustomScreen("revanced_misc_screen")

    internal class CustomScreen(key: String) : Screen(key) {
        /* Categories */
        val GENERAL = CustomCategory("revanced_general_category")
        val OTHER = CustomCategory("revanced_other_category")
        val CLIENT_SIDE = CustomCategory("revanced_client_ads_category")
        val SURESTREAM = CustomCategory("revanced_surestream_ads_category")

        internal inner class CustomCategory(key: String) : Category(key) {
            /* For Twitch, we need to load our CustomPreferenceCategory class instead of the default one. */
            override fun transform(): PreferenceCategory = PreferenceCategory(
                key,
                preferences = preferences,
                tag = "app.revanced.extension.twitch.settings.preference.CustomPreferenceCategory",
            )
        }
    }

    override fun commit(screen: app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference) {
        preferences += screen
    }
}
