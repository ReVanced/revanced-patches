package com.strava.modularframework.data;

public final class ItemIdentifier {
    private final String compoundId;
    private final String id;
    private final String type;

    public ItemIdentifier(String type, String id) {
        this.type = type;
        this.id = id;
        this.compoundId = null;
    }

    public final String getCompoundId() {
        return compoundId;
    }

    public final String getId() {
        return id;
    }

    public final String getType() {
        return type;
    }
}
