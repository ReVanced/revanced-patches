package app.revanced.extension.spotify.shared;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

public final class ComponentFilters {

    public interface ComponentFilter {
        @NonNull
        String getFilterValue();
        String getFilterRepresentation();
        default boolean filterUnavailable() {
            return false;
        }
    }

    public static final class ResourceIdComponentFilter implements ComponentFilter {

        public final String resourceName;
        public final String resourceType;
        // Android resources are always positive, so -1 is a valid sentinel value to indicate it has not been loaded.
        // 0 is returned when a resource has not been found.
        private int resourceId = -1;
        @Nullable
        private String stringfiedResourceId;

        public ResourceIdComponentFilter(String resourceName, String resourceType) {
            this.resourceName = resourceName;
            this.resourceType = resourceType;
        }

        public int getResourceId() {
            if (resourceId == -1) {
                resourceId = Utils.getResourceIdentifier(resourceName, resourceType);
            }
            return resourceId;
        }

        @NonNull
        @Override
        public String getFilterValue() {
            if (stringfiedResourceId == null) {
                stringfiedResourceId = Integer.toString(getResourceId());
            }
            return stringfiedResourceId;
        }

        @Override
        public String getFilterRepresentation() {
            boolean resourceFound = getResourceId() != 0;
            return (resourceFound ? getFilterValue() + " (" : "") + resourceName + (resourceFound ? ")" : "");
        }

        @Override
        public boolean filterUnavailable() {
            boolean resourceNotFound = getResourceId() == 0;
            if (resourceNotFound) {
                Logger.printInfo(() -> "Resource id for " + resourceName + " was not found");
            }
            return resourceNotFound;
        }
    }

    public static final class StringComponentFilter implements ComponentFilter {

        public final String string;

        public StringComponentFilter(String string) {
            this.string = string;
        }

        @NonNull
        @Override
        public String getFilterValue() {
            return string;
        }

        @Override
        public String getFilterRepresentation() {
            return string;
        }
    }
}
