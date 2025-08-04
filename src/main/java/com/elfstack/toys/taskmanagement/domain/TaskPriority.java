package com.elfstack.toys.taskmanagement.domain;

public enum TaskPriority {
    FAIBLE("Faible"),
    NORMALE("Normale"),
    ELEVEE("Élevée"),
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