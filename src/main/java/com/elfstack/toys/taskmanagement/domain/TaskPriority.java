package com.elfstack.toys.taskmanagement.domain;

public enum TaskPriority {
    TRES_BASSE("Très basse"),
    BASSE("Basse"),
    MOYENNE("Moyenne"),
    HAUTE("Haute"),
    CRITIQUE("Critique");

    private final String displayName;

    TaskPriority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}