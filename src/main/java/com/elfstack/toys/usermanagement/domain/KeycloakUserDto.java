package com.elfstack.toys.usermanagement.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KeycloakUserDto {

    // Getters et Setters
    @Getter
    private String id;
    @Getter
    private String username;
    @Getter
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private boolean emailVerified;
    private long createdTimestamp;
    private List<String> requiredActions;
    private List<String> groups;
    private Map<String, List<String>> attributes;

    // Constructeurs
    public KeycloakUserDto() {}

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @JsonProperty("emailVerified")
    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public List<String> getRequiredActions() {
        return requiredActions;
    }

    public void setRequiredActions(List<String> requiredActions) {
        this.requiredActions = requiredActions;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
    }

    /**
     * Retourne le nom complet de l'utilisateur
     */
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        if (firstName != null && !firstName.trim().isEmpty()) {
            fullName.append(firstName.trim());
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(lastName.trim());
        }
        return fullName.length() > 0 ? fullName.toString() : username;
    }

    @Override
    public String toString() {
        return "KeycloakUserDto{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}