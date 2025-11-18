package com.walrex.gateway.gateway.infrastructure.adapters.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("gateway.tb_modules")
public class ModulesUrl {
    @Id
    private Long id;
    @Column("module_name")
    private String moduleName;
    private String uri;
    private String path;
    private Integer stripPrefixCount;
    private String status;
    @Column("ispattern")
    private Boolean isPattern;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
