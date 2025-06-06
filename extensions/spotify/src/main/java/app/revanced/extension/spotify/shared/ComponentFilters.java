package app.revanced.extension.spotify.shared;

import app.revanced.extension.shared.Utils;

public final class ComponentFilters {

    public interface ComponentFilter {
        String getFilterValue();
        String getFilterRepresentation();
    }

    public static final class ResourceIdComponentFilter implements ComponentFilter {

        public final String resourceName;
        public final String resourceType;
        private int resourceId = -1;
        private String stringfiedResourceId = null;

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

        @Override
        public String getFilterValue() {
            if (stringfiedResourceId == null) {
                stringfiedResourceId = Integer.toString(getResourceId());
            }
            return stringfiedResourceId;
        }

        @Override
        public String getFilterRepresentation() {
            return getFilterValue() + " (" + resourceName + ")";
        }
    }

    public static final class StringComponentFilter implements ComponentFilter {

        public final String string;

        public StringComponentFilter(String string) {
            this.string = string;
        }

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
