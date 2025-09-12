package com.walrex.module_driver.domain.model.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchDriverByParameters {
    private String numDoc;
    private Integer idTipDoc;
    private String name;
}
