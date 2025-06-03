package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper;

import com.walrex.module_almacen.domain.model.Almacen;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.OrdenIngresoEntity;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrdenIngresoEntityMapper {
    OrdenIngresoEntityMapper INSTANCE = Mappers.getMapper(OrdenIngresoEntityMapper.class);

    @Mapping(source = "id", target = "id_ordeningreso", qualifiedByName = "integerToLong")
    @Mapping(source = "idCliente", target = "id_cliente")
    @Mapping(source = "motivo.idMotivo", target = "id_motivo")
    @Mapping(source = "idOrigen", target = "id_origen")
    @Mapping(source = "comprobante", target = "id_comprobante")
    @Mapping(source = "nroComprobante", target = "nu_comprobante")
    @Mapping(source = "fechaIngreso", target = "fec_ingreso")
    @Mapping(source = "fechaComprobante", target = "fec_referencia")
    @Mapping(source = "codSerie", target = "nu_serie")
    @Mapping(source = "almacen.idAlmacen", target = "id_almacen")
    @Mapping(source = "comprobanteRef", target = "comprobante_ref")
    @Mapping(source = "idOrdServ", target = "idOrdenServ")
    OrdenIngresoEntity toEntity(OrdenIngreso ordenIngreso);

    @InheritInverseConfiguration(name = "toEntity")
    @Mapping(source = "id_ordeningreso", target = "id", qualifiedByName = "longToInteger")
    @Mapping(source = "fec_ingreso", target = "fechaIngreso", qualifiedByName = "dateToLocalDate")
    @Mapping(source = "fec_referencia", target = "fechaComprobante", qualifiedByName = "dateToLocalDate")
    @Mapping(target = "almacen", expression = "java(buildAlmacen(entity.getId_almacen()))")
    //@Mapping(target = "detalles", ignore = true)
    OrdenIngreso toDomain(OrdenIngresoEntity entity);

    @Named("integerToLong")
    default Long integerToLong(Integer value) {
        return value != null ? value.longValue() : null;
    }

    @Named("longToInteger")
    default Integer longToInteger(Long value) {
        if (value == null) {
            return null;
        }
        return value.intValue();
    }

    @Named("dateToLocalDate")
    default LocalDate dateToLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Named("localDateToDate") //
    default Date localDateToDate(LocalDate localDate) { //
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
    
    // Método auxiliar para construir el objeto Almacen
    default Almacen buildAlmacen(Integer idAlmacen) {
        if (idAlmacen == null) {
            return null;
        }
        return Almacen.builder()
                .idAlmacen(idAlmacen)
                .build();
    }
}
