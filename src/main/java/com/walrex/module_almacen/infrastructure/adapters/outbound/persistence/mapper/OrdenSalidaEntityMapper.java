package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.module_almacen.domain.model.dto.OrdenEgresoDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.OrdenSalidaEntity;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrdenSalidaEntityMapper {

    @Mapping(source = "motivo.idMotivo", target = "id_motivo")
    @Mapping(source = "isInterno", target = "is_interno")
    @Mapping(source = "idTipoComprobante", target = "id_tipo_comprobante")
    @Mapping(source = "numComprobante", target = "num_comprobante")
    @Mapping(source = "almacenOrigen.idAlmacen", target = "id_store_source")
    @Mapping(source = "almacenDestino.idAlmacen", target = "id_store_target")
    @Mapping(source = "fecRegistro", target = "create_at", qualifiedByName = "mapLocalDateToOffsetDateTime")
    OrdenSalidaEntity toEntity(OrdenEgresoDTO dto);

    @Mapping(source = "id_motivo", target = "motivo.idMotivo")
    @Mapping(source = "is_interno", target = "isInterno")
    @Mapping(source = "id_tipo_comprobante", target = "idTipoComprobante")
    @Mapping(source = "num_comprobante", target = "numComprobante")
    @Mapping(source = "id_store_source", target = "almacenOrigen.idAlmacen")
    @Mapping(source = "id_store_target", target = "almacenDestino.idAlmacen")
    @Mapping(source = "create_at", target = "fecRegistro", qualifiedByName = "mapOffsetDateTimeToLocalDate")
    @Mapping(source = "id_usuario", target = "idUsuario")
    @Mapping(source = "fec_entrega", target = "fecEntrega")
    @Mapping(source = "id_user_entrega", target = "idUsuarioEntrega")
    @Mapping(source = "id_documento_ref", target = "idDocumentoRef")
    @Mapping(source = "cod_salida", target = "codEgreso")
    @Mapping(source = "id_cliente", target = "idCliente")
    @Mapping(source = "id_requerimiento", target = "idRequerimiento")
    @Mapping(source = "id_supervisor", target = "idSupervisor")
    @Mapping(source = "correlativo_motivo", target = "correlativoMotivo")
    OrdenEgresoDTO toDomain(OrdenSalidaEntity entity);

    @Named("mapLocalDateToOffsetDateTime")
    default OffsetDateTime mapLocalDateToOffsetDateTime(LocalDate date) {
        if (date == null) {
            return null;
        }
        // Combina la LocalDate con la hora de medianoche (00:00:00) para obtener un LocalDateTime
        LocalDateTime localDateTime = date.atStartOfDay();
        // Convierte el LocalDateTime a OffsetDateTime usando ZoneOffset.UTC
        return localDateTime.atOffset(ZoneOffset.UTC);
    }

    @Named("mapOffsetDateTimeToLocalDate")
    default LocalDate mapOffsetDateTimeToLocalDate(OffsetDateTime offsetDateTime){
        return offsetDateTime != null ? offsetDateTime.toLocalDate() : null;
    }
}
