package app.revanced.extension.shared;

import java.util.HashMap;
import java.util.Map;

public enum ResourceType {
    ANIM("anim"),
    ANIMATOR("animator"),
    ARRAY("array"),
    ATTR("attr"),
    BOOL("bool"),
    COLOR("color"),
    DIMEN("dimen"),
    DRAWABLE("drawable"),
    FONT("font"),
    FRACTION("fraction"),
    ID("id"),
    INTEGER("integer"),
    INTERPOLATOR("interpolator"),
    LAYOUT("layout"),
    MENU("menu"),
    MIPMAP("mipmap"),
    NAVIGATION("navigation"),
    PLURALS("plurals"),
    RAW("raw"),
    STRING("string"),
    STYLE("style"),
    STYLEABLE("styleable"),
    TRANSITION("transition"),
    VALUES("values"),
    XML("xml");

    private static final Map<String, ResourceType> VALUE_MAP;

    static {
        ResourceType[] values = values();
        VALUE_MAP = new HashMap<>(2 * values.length);

        for (ResourceType type : values) {
            VALUE_MAP.put(type.value, type);
        }
    }

    public final String value;

    public static ResourceType fromValue(String value) {
        ResourceType type = VALUE_MAP.get(value);
        if (type == null) {
            throw new IllegalArgumentException("Unknown resource type: " + value);
        }
        return type;
    }

    ResourceType(String value) {
        this.value = value;
    }
}
