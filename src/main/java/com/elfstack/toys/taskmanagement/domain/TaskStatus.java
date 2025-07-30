package com.elfstack.toys.taskmanagement.domain;

public enum TaskStatus {
    NOUVEAU("Nouveau"),
    EN_COURS("En cours"),
    PENDING("En attente"),
    EN_REVISION("En révision"),
    TERMINE("Terminé"),
    ANNULE("Annulé"),;

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public String getBadgeVariant() {
        return "";
    }
}