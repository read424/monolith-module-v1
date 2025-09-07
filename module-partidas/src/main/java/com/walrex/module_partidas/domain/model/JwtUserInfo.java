package com.walrex.module_partidas.domain.model;

import java.util.Arrays;
import java.util.List;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtUserInfo {
    private String userId;
    private String username;
    private String userRoleId;
    private String userRole;
    private String employeeName;
    private List<String> permissions;
    private String audience;
    private boolean hasAuthHeader;

    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    public boolean hasAnyPermission(String... permissions) {
        if (this.permissions == null || this.permissions.isEmpty()) {
            return false;
        }
        return Arrays.stream(permissions).anyMatch(this.permissions::contains);
    }

    public boolean isValid() {
        return userId != null && username != null;
    }
}

