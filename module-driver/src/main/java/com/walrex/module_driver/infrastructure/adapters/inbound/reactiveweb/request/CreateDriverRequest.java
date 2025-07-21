package com.walrex.module_driver.infrastructure.adapters.inbound.reactiveweb.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDriverRequest {
    @NotNull(message = "El tipo de documento es obligatorio")
    @JsonProperty("id_tipo_documento")
    private Integer idTipoDocumento;

    @NotBlank(message = "El numero de documento es obligatorio")
    @Size(max = 15)
    @JsonProperty("num_documento")
    private String numDocumento;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 65, message = "El apellido debe tener menos de 65 caracteres")
    @JsonProperty("apellidos_chofer")
    private String lastName;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 65, message = "El nombre debe tener menos de 65 caracteres")
    @JsonProperty("nombres_chofer")
    private String firstName;

    @NotBlank(message = "El numero de licencia es obligatorio")
    @Size(max = 15)
    @JsonProperty("num_licencia")
    private String numLicencia;
}
