package com.walrex.user.module_users.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalItem {

    @JsonProperty("id_personal")
    private Integer idPersonal;

    @JsonProperty("no_apepat")
    private String noApepat;

    @JsonProperty("no_apemat")
    private String noApemat;

    @JsonProperty("no_nombres")
    private String noNombres;

    @JsonProperty("nu_doc")
    private String nuDoc;

    @JsonProperty("id_det_personal")
    private Integer idDetPersonal;

    @JsonProperty("id_area")
    private Integer idArea;

    @JsonProperty("no_area")
    private String noArea;

    @JsonProperty("id_cargo")
    private Integer idCargo;

    @JsonProperty("id_status")
    private Integer idStatus;
}
