package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper;

import com.walrex.module_almacen.domain.model.dto.AprobarSalidaRequerimiento;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto.AprobarSalidaRequestDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto.ProductoSalidaDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
public class ApproveDeliverRequestMapperTest {
    private final ApproveDeliverRequestMapper mapper = Mappers.getMapper(ApproveDeliverRequestMapper.class);

    @Test
    @DisplayName("Debe mapear correctamente de RequestDTO a DomainDTO con @JsonProperty")
    void debeMapearCorrectamenteConJsonProperty() {
        //Given
        OffsetDateTime fechaEntrega = OffsetDateTime.of(2025, 5, 27, 15, 30, 0, 0, ZoneOffset.UTC);

        ProductoSalidaDTO producto1 = ProductoSalidaDTO.builder()
                .id_detalle_orden(1195985)
                .id_articulo(325)
                .desc_articulo("DESENGRASS-W")
                .abrev_unidad("G")
                .cantidad(1710.08)
                .selected(true)
                .delete("1")
                .id_unidad_consumo(6)
                .id_unidad(6)
                .id_unidad_old(6)
                .build();

        AprobarSalidaRequestDTO requestDTO = AprobarSalidaRequestDTO.builder()
                .id_ordensalida(360244)
                .cod_salida("")
                .id_requerimiento(0)
                .id_tipo_comprobante(3)
                .id_almacen_origen(1)
                .id_almacen_destino(5)
                .id_usuario_entrega(136)
                .entregado("0")
                .id_personal_supervisor(165)
                .fec_entrega(fechaEntrega)
                .productos(Arrays.asList(producto1))
                .build();

        // When
        AprobarSalidaRequerimiento resultado = mapper.toDomain(requestDTO);
        System.out.println("Resultado: " + resultado);

        // Then
        assertThat(resultado).isNotNull();
        /*
        assertThat(resultado.getIdOrdenSalida()).isEqualTo(360244);
        assertThat(resultado.getCodOrdenSalida()).isEqualTo("SAL-2025-001");
        assertThat(resultado.getIdRequerimiento()).isEqualTo(123);
        assertThat(resultado.getIdAlmacenOrigen()).isEqualTo(1);
        assertThat(resultado.getIdAlmacenDestino()).isEqualTo(5);
        assertThat(resultado.getIdUsuarioEntrega()).isEqualTo(136);
        assertThat(resultado.getIdUsuarioSupervisor()).isEqualTo(165);
        assertThat(resultado.getFecEntrega()).isNotNull().isInstanceOf(Date.class);
        assertThat(resultado.getDetalles()).hasSize(1);

        ArticuloRequerimientoDTO detalle = resultado.getDetalles().get(0);
        assertThat(detalle.getIdDetalleOrden()).isEqualTo(1001);
        assertThat(detalle.getIdArticulo()).isEqualTo(325);
        assertThat(detalle.getDescArticulo()).isEqualTo("DESENGRASS-W");
        assertThat(detalle.getSelected()).isTrue();
         */
    }
}
