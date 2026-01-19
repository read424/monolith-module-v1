package com.walrex.module_partidas.infrastructure.adapters.inbound.reactiveweb.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcesosPartidaIncompletosRequest {

    @NotNull(message = "El id_partida no puede ser nulo")
    @JsonProperty("id_partida")
    private Integer idPartida;

    @NotNull(message = "El arreglo de procesos no puede ser nulo")
    @NotEmpty(message = "El arreglo de procesos no puede estar vacío")
    @Valid
    private ProcesoIncompletoRequest[] procesos;
}
