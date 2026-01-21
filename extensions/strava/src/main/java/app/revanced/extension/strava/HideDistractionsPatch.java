package app.revanced.extension.strava;

import android.annotation.SuppressLint;

import com.strava.modularframework.data.Destination;
import com.strava.modularframework.data.GenericLayoutModule;
import com.strava.modularframework.data.GenericModuleField;
import com.strava.modularframework.data.ListField;
import com.strava.modularframework.data.ListProperties;
import com.strava.modularframework.data.ModularComponent;
import com.strava.modularframework.data.ModularEntry;
import com.strava.modularframework.data.ModularEntryContainer;
import com.strava.modularframework.data.ModularMenuItem;
import com.strava.modularframework.data.Module;
import com.strava.modularframework.data.MultiStateFieldDescriptor;
import com.strava.modularframeworknetwork.ModularEntryNetworkContainer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressLint("NewApi")
public class HideDistractionsPatch {
    public static boolean upselling;
    public static boolean promo;
    public static boolean followSuggestions;
    public static boolean challengeSuggestions;
    public static boolean joinChallenge;
    public static boolean joinClub;
    public static boolean activityLookback;

    public static List<ModularEntry> filterChildrenEntries(ModularEntry modularEntry) {
        if (hideModularEntry(modularEntry)) {
            return Collections.emptyList();
        }
        return modularEntry.getChildrenEntries$original().stream()
                .filter(childrenEntry -> !hideModularEntry(childrenEntry))
                .collect(Collectors.toList());
    }

    public static List<ModularEntry> filterEntries(ModularEntryContainer modularEntryContainer) {
        if (hideModularEntryContainer(modularEntryContainer)) {
            return Collections.emptyList();
        }
        return modularEntryContainer.getEntries$original().stream()
                .filter(entry -> !hideModularEntry(entry))
                .collect(Collectors.toList());
    }

    public static List<ModularEntry> filterEntries(ModularEntryNetworkContainer modularEntryNetworkContainer) {
        if (hideModularEntryNetworkContainer(modularEntryNetworkContainer)) {
            return Collections.emptyList();
        }
        return modularEntryNetworkContainer.getEntries$original().stream()
                .filter(entry -> !hideModularEntry(entry))
                .collect(Collectors.toList());
    }

    public static List<ModularMenuItem> filterMenuItems(ModularEntryContainer modularEntryContainer) {
        if (hideModularEntryContainer(modularEntryContainer)) {
            return Collections.emptyList();
        }
        return modularEntryContainer.getMenuItems$original().stream()
                .filter(menuItem -> !hideModularMenuItem(menuItem))
                .collect(Collectors.toList());
    }

    public static ListProperties filterProperties(ModularEntryContainer modularEntryContainer) {
        if (hideModularEntryContainer(modularEntryContainer)) {
            return null;
        }
        return modularEntryContainer.getProperties$original();
    }

    public static ListProperties filterProperties(ModularEntryNetworkContainer modularEntryNetworkContainer) {
        if (hideModularEntryNetworkContainer(modularEntryNetworkContainer)) {
            return null;
        }
        return modularEntryNetworkContainer.getProperties$original();
    }

    public static ListField filterField(ListProperties listProperties, String key) {
        ListField listField = listProperties.getField$original(key);
        if (hideListField(listField)) {
            return null;
        }
        return listField;
    }

    public static List<ListField> filterFields(ListField listField) {
        if (hideListField(listField)) {
            return null;
        }
        return listField.getFields$original().stream()
                .filter(field -> !hideListField(field))
                .collect(Collectors.toList());
    }

    public static List<Module> filterModules(ModularEntry modularEntry) {
        if (hideModularEntry(modularEntry)) {
            return Collections.emptyList();
        }
        return modularEntry.getModules$original().stream()
                .filter(module -> !hideModule(module))
                .collect(Collectors.toList());
    }

    public static GenericModuleField filterField(GenericLayoutModule genericLayoutModule, String key) {
        if (hideGenericLayoutModule(genericLayoutModule)) {
            return null;
        }
        GenericModuleField field = genericLayoutModule.getField$original(key);
        if (hideGenericModuleField(field)) {
            return null;
        }
        return field;
    }

    public static GenericModuleField[] filterFields(GenericLayoutModule genericLayoutModule) {
        if (hideGenericLayoutModule(genericLayoutModule)) {
            return new GenericModuleField[0];
        }
        return Arrays.stream(genericLayoutModule.getFields$original())
                .filter(field -> !hideGenericModuleField(field))
                .toArray(GenericModuleField[]::new);
    }

    public static GenericLayoutModule[] filterSubmodules(GenericLayoutModule genericLayoutModule) {
        if (hideGenericLayoutModule(genericLayoutModule)) {
            return new GenericLayoutModule[0];
        }
        return Arrays.stream(genericLayoutModule.getSubmodules$original())
                .filter(submodule -> !hideGenericLayoutModule(submodule))
                .toArray(GenericLayoutModule[]::new);
    }

    public static List<Module> filterSubmodules(ModularComponent modularComponent) {
        if (hideByName(modularComponent.getPage()) || hideByName(modularComponent.getElement())) {
            return Collections.emptyList();
        }
        return modularComponent.getSubmodules$original().stream()
                .filter(submodule -> !hideModule(submodule))
                .collect(Collectors.toList());
    }

    public static Map<String, GenericModuleField> filterStateMap(MultiStateFieldDescriptor multiStateFieldDescriptor) {
        return multiStateFieldDescriptor.getStateMap$original().entrySet().stream()
                .filter(entry -> !hideGenericModuleField(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static boolean hideModule(Module module) {
        return module == null ||
                hideByName(module.getPage()) ||
                hideByName(module.getElement());
    }

    private static boolean hideModularEntry(ModularEntry modularEntry) {
        return modularEntry == null ||
                hideByName(modularEntry.getPage()) ||
                hideByName(modularEntry.getElement()) ||
                hideByDestination(modularEntry.getDestination());
    }

    private static boolean hideGenericLayoutModule(GenericLayoutModule genericLayoutModule) {
        try {
            return genericLayoutModule == null ||
                    hideByName(genericLayoutModule.getPage()) ||
                    hideByName(genericLayoutModule.getElement()) ||
                    hideByDestination(genericLayoutModule.getDestination());
        } catch (RuntimeException getParentEntryOrThrowException) {
            return false;
        }
    }

    private static boolean hideListField(ListField listField) {
        return listField == null ||
                hideByName(listField.getElement()) ||
                hideByDestination(listField.getDestination());
    }

    private static boolean hideGenericModuleField(GenericModuleField genericModuleField) {
        return genericModuleField == null ||
                hideByName(genericModuleField.getElement()) ||
                hideByDestination(genericModuleField.getDestination());
    }

    private static boolean hideModularEntryContainer(ModularEntryContainer modularEntryContainer) {
        return modularEntryContainer == null ||
                hideByName(modularEntryContainer.getPage());
    }

    private static boolean hideModularEntryNetworkContainer(ModularEntryNetworkContainer modularEntryNetworkContainer) {
        return modularEntryNetworkContainer == null ||
                hideByName(modularEntryNetworkContainer.getPage());
    }

    private static boolean hideModularMenuItem(ModularMenuItem modularMenuItem) {
        return modularMenuItem == null ||
                hideByName(modularMenuItem.getElementName()) ||
                hideByDestination(modularMenuItem.getDestination());
    }

    private static boolean hideByName(String name) {
        return name != null && (
                upselling && name.contains("_upsell") ||
                        promo && (name.equals("promo") || name.equals("top_of_tab_promo")) ||
                        followSuggestions && name.equals("suggested_follows") ||
                        challengeSuggestions && name.equals("suggested_challenges") ||
                        joinChallenge && name.equals("challenge") ||
                        joinClub && name.equals("club") ||
                        activityLookback && name.equals("highlighted_activity_lookback")
        );
    }

    private static boolean hideByDestination(Destination destination) {
        if (destination == null) {
            return false;
        }
        String url = destination.getUrl();
        return url != null && (
                upselling && url.startsWith("strava://subscription/checkout")
        );
    }
}
