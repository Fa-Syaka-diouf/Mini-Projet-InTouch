package com.elfstack.toys.taskmanagement.domain;


import java.util.Arrays;
import java.util.List;

public enum StatutEnum {
    EN_COURS("En Cours"),
    A_FAIRE("À Faire"),
    SUSPENDU("Annulé"),
    TERMINER("Terminé");
    private final String value;

    StatutEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
