package com.brainlessguidehelper.models;

import lombok.Data;

@Data
public class Requirement {
    private RequirementType type;
    private Object id; // Can be String or Integer
    private Integer amount;
    private String status;
    private Integer level;
    private ItemLocation location;

    public enum RequirementType {
        SKILL, QUEST, ITEM, DIARY, VARBIT, VARP
    }

    public enum ItemLocation {
        INVENTORY, BANK, EQUIPPED, ANY
    }
}
