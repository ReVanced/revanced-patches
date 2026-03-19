package com.amazon.digitalmusicxp.types;

import com.amazon.digitalmusicxp.enums.QueueEntityIdTypeEnum;

@SuppressWarnings("unused")
public final class GenericQueueEntity {
    private String entityReferenceId;
    private QueueEntityIdTypeEnum identifierType;

    public String getEntityReferenceId() { return entityReferenceId; }
    public QueueEntityIdTypeEnum getIdentifierType() { return identifierType; }

}