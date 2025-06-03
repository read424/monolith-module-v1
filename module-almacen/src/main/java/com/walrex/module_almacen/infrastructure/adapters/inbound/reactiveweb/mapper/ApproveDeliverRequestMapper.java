package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper;

import com.walrex.module_almacen.domain.model.dto.AprobarSalidaRequerimiento;
import com.walrex.module_almacen.domain.model.dto.ArticuloRequerimiento;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto.AprobarSalidaRequestDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto.ProductoSalidaDTO;
import org.mapstruct.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ApproveDeliverRequestMapper {
    @Mapping(source = "id_ordensalida", target = "idOrdenSalida")
    @Mapping(source = "cod_salida", target = "codOrdenSalida")
    @Mapping(source = "id_requerimiento", target = "idRequerimiento")
    @Mapping(source = "id_tipo_comprobante", target = "idTipoComprobante")
    @Mapping(source = "id_almacen_origen", target = "idAlmacenOrigen")
    @Mapping(source = "id_almacen_destino", target = "idAlmacenDestino")
    @Mapping(source = "id_usuario_entrega", target = "idUsuarioEntrega")
    @Mapping(source = "id_personal_supervisor", target = "idUsuarioSupervisor")
    @Mapping(source = "fec_entrega", target = "fecEntrega", qualifiedByName = "offsetDateTimeToDate")
    @Mapping(source = "productos", target = "detalles")
    AprobarSalidaRequerimiento toDomain(AprobarSalidaRequestDTO dto);

    @Mapping(source = "id_detalle_orden", target = "idDetalleOrden")
    @Mapping(source = "id_articulo", target = "idArticulo")
    @Mapping(source = "desc_articulo", target = "descArticulo")
    @Mapping(source = "abrev_unidad", target = "abrevUnidad")
    @Mapping(source = "delete", target = "deleted")
    @Mapping(source = "id_unidad_consumo", target = "idUnidadConsumo")
    @Mapping(source = "id_unidad", target = "idUnidad")
    @Mapping(source = "id_unidad_old", target = "idUnidadOld")
    ArticuloRequerimiento toArticuloRequerimiento(ProductoSalidaDTO producto);

    // ✅ Método auxiliar para la conversión
    @Named("offsetDateTimeToDate")
    default Date convertirOffsetDateTimeADate(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return Date.from(offsetDateTime.toInstant());
    }

    // ✅ Conversión inversa Date → OffsetDateTime
    @Named("DateToOffsetDateTime")
    default OffsetDateTime map(Date date) {
        if (date == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
    }
}
