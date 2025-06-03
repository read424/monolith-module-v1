package com.walrex.module_core.security;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SecurityContext {
    private String userId;
    private String email;
    private List<String> roles;
    private String tenantId;
}
