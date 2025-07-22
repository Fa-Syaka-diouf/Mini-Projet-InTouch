package com.elfstack.toys.taskmanagement.domain;


import java.util.Arrays;
import java.util.List;

public enum StatutEnum {
    EN_COURS("En cours"),
    A_FAIRE("A faire"),
    SUSPENDU("Suspendu"),
    TERMINER("Terminer"),;
    private final String value;

    StatutEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
