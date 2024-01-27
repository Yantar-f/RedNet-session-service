package com.rednet.sessionservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.bouncycastle.util.Arrays;

import java.util.HashSet;
import java.util.List;

public record SessionCreationData(
        @NotEmpty(message = "UserID should be not blank")
        String userID,

        @NotEmpty( message = "There is should be at least one role")
        String[] roles
) {
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;

        SessionCreationData data = (SessionCreationData) obj;

        return  userID.equals(data.userID) &&
                roles.length == data.roles.length &&
                new HashSet<>(List.of(roles)).containsAll(List.of(data.roles));
    }

    @Override
    public int hashCode() {
        return userID.hashCode() * Arrays.hashCode(roles);
    }
}
