package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import com.walrex.module_almacen.domain.model.dto.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.*;

/**
 * Mapper entre DTOs de dominio y entidades de persistencia para devoluciones de
 * rollos
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SalidaDevolucionEntityMapper {

    SalidaDevolucionEntityMapper INSTANCE = Mappers.getMapper(SalidaDevolucionEntityMapper.class);

    /**
     * Convierte SalidaDevolucionDTO a OrdenSalidaEntity
     */
    @Mapping(source = "idMotivo", target = "id_motivo")
    @Mapping(target = "is_interno", constant = "0")
    @Mapping(source = "idAlmacenOrigen", target = "id_store_source")
    @Mapping(source = "idAlmacenDestino", target = "id_store_target")
    @Mapping(source = "fechaRegistro", target = "create_at", qualifiedByName = "localDateToOffsetDateTime")
    @Mapping(source = "idUsuario", target = "id_usuario")
    @Mapping(target = "entregado", constant = "1")
    @Mapping(source = "idCliente", target = "id_cliente")
    @Mapping(source = "observacion", target = "observacion")
    OrdenSalidaEntity toOrdenSalidaEntity(SalidaDevolucionDTO salidaDevolucion);

    /**
     * Convierte lista de DevolucionArticuloDTO a DetailSalidaEntity
     */
    @Mapping(source = "idOrdenSalida", target = "id_ordensalida")
    @Mapping(source = "idArticulo", target = "id_articulo")
    @Mapping(source = "idUnidad", target = "id_unidad")
    @Mapping(source = "cantidad", target = "cantidad", qualifiedByName = "integerToDouble")
    @Mapping(source = "totalPeso", target = "tot_kilos", qualifiedByName = "bigDecimalToDouble")
    @Mapping(target = "tot_monto", constant = "0.0")
    @Mapping(target = "precio", constant = "0.0")
    DetailSalidaEntity toDetailSalidaEntity(DevolucionArticuloDTO articulos);

    /**
     * Convierte lista de RolloDevolucionDTO a DetailOrdenSalidaPesoEntity
     */
    @Mapping(source = "idDetOrdenIngresoPeso", target = "idRolloIngreso")
    DetailOrdenSalidaPesoEntity toDetailOrdenSalidaPesoEntity(RolloDevolucionDTO rollos);

    /**
     * Convierte RolloDevolucionDTO a DevolucionRollosEntity para trazabilidad
     */
    @Mapping(source = "idDetOrdenSalidaPeso", target = "idDetOrdenSalidaPeso")
    @Mapping(source = "idRolloIngresoAlmacen", target = "idOrdenIngreso")
    @Mapping(source = "idDetOrdenIngresoAlmacen", target = "idDetOrdenIngreso")
    @Mapping(source = "idDetOrdenIngresoPesoAlmacen", target = "idDetOrdenIngresoPeso")
    DevolucionRollosEntity toDevolucionRollosEntity(RolloDevolucionDTO rollos);

    /**
     * Convierte OrdenSalidaEntity a SalidaDevolucionDTO
     */
    @Mapping(source = "id", target = "idOrdenSalida")
    @Mapping(source = "id_motivo", target = "idMotivo")
    @Mapping(source = "id_store_source", target = "idAlmacenOrigen")
    @Mapping(source = "id_store_target", target = "idAlmacenDestino")
    @Mapping(source = "id_cliente", target = "idCliente")
    @Mapping(source = "id_usuario", target = "idUsuario")
    @Mapping(source = "cod_salida", target = "codSalida")
    @Mapping(source = "observacion", target = "observacion")
    @Mapping(source = "create_at", target = "fechaRegistro", qualifiedByName = "offsetDateTimeToLocalDate")
    @Mapping(target = "articulos", ignore = true) // Se mapea por separado
    SalidaDevolucionDTO toSalidaDevolucionDTO(OrdenSalidaEntity ordenSalida);

    /**
     * Convierte DetailSalidaEntity a DevolucionArticuloDTO
     */
    @Mapping(source = "id_articulo", target = "idArticulo")
    @Mapping(source = "id_unidad", target = "idUnidad")
    @Mapping(source = "cantidad", target = "cantidad", qualifiedByName = "doubleToInteger")
    @Mapping(source = "tot_kilos", target = "totalPeso", qualifiedByName = "doubleToBigDecimal")
    @Mapping(target = "codArticulo", ignore = true) // Se obtiene de otra consulta
    @Mapping(target = "descArticulo", ignore = true) // Se obtiene de otra consulta
    @Mapping(target = "statusArticulo", ignore = true) // Se obtiene de otra consulta
    @Mapping(target = "rollos", ignore = true) // Se mapea por separado
    List<DevolucionArticuloDTO> toDevolucionArticuloDTOs(List<DetailSalidaEntity> detailSalidas);

    /**
     * Convierte DetailOrdenSalidaPesoEntity a RolloDevolucionDTO
     */
    @Mapping(source = "codRollo", target = "codRollo")
    @Mapping(source = "pesoRollo", target = "pesoRollo")
    @Mapping(source = "idDetPartida", target = "idDetPartida")
    @Mapping(source = "idRolloIngreso", target = "idRolloIngresoAlmacen") // Mapeo específico
    @Mapping(target = "idRolloIngreso", ignore = true) // Se obtiene de relaciones
    @Mapping(target = "idDetOrdenIngreso", ignore = true) // Se obtiene de relaciones
    @Mapping(target = "idDetOrdenIngresoPeso", ignore = true) // Se obtiene de relaciones
    @Mapping(target = "statusRollIngreso", ignore = true) // Se obtiene de relaciones
    @Mapping(target = "sinCobro", ignore = true) // Se obtiene de relaciones
    @Mapping(target = "statusRollPartida", ignore = true) // Se obtiene de relaciones
    @Mapping(target = "idDetOrdenIngresoAlmacen", ignore = true) // Se obtiene de relaciones
    @Mapping(target = "idDetOrdenIngresoPesoAlmacen", ignore = true) // Se obtiene de relaciones
    @Mapping(target = "statusRollIngresoPesoAlmacen", ignore = true) // Se obtiene de relaciones
    @Mapping(target = "deleted", constant = "false")
    List<RolloDevolucionDTO> toRolloDevolucionDTOs(List<DetailOrdenSalidaPesoEntity> detallesPeso);

    // ===== MÉTODOS DE CONVERSIÓN =====

    @Named("localDateToOffsetDateTime")
    default OffsetDateTime localDateToOffsetDateTime(LocalDate localDate) {
        return localDate != null ? localDate.atStartOfDay(java.time.ZoneOffset.UTC).toOffsetDateTime() : null;
    }

    @Named("offsetDateTimeToLocalDate")
    default LocalDate offsetDateTimeToLocalDate(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toLocalDate() : null;
    }

    @Named("integerToDouble")
    default Double integerToDouble(Integer value) {
        return value != null ? value.doubleValue() : null;
    }

    @Named("doubleToInteger")
    default Integer doubleToInteger(Double value) {
        return value != null ? value.intValue() : null;
    }

    @Named("bigDecimalToDouble")
    default Double bigDecimalToDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : null;
    }

    @Named("doubleToBigDecimal")
    default BigDecimal doubleToBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }
}