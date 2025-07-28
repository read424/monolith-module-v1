package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.*;

import com.walrex.module_ecomprobantes.domain.model.dto.*;
import com.walrex.module_ecomprobantes.domain.model.dto.lycet.*;

/**
 * Mapper para transformar ReferralGuideDTO a LycetGuiaRemisionRequest.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LycetMapper {

    @Mapping(target = "version", source = "idVersion")
    @Mapping(target = "tipoDoc", constant = "09")
    @Mapping(target = "serie", source = "codSerie")
    @Mapping(target = "correlativo", source = "numCorrelativo")
    @Mapping(target = "observacion", source = "observacion")
    @Mapping(target = "fechaEmision", source = "fecEmision", qualifiedByName = "localDateToLocalDateTime")
    @Mapping(target = "company", source = "company")
    @Mapping(target = "destinatario", source = "receiver")
    @Mapping(target = "envio", source = "shipment")
    @Mapping(target = "details", source = "detalle")
    LycetGuiaRemisionRequest toLycetRequest(ReferralGuideDTO referralGuideDTO);

    @Mapping(target = "ruc", source = "numDocumento")
    @Mapping(target = "razonSocial", source = "razonSocial")
    @Mapping(target = "nombreComercial", source = "nombreComercial")
    LycetCompany toLycetCompany(CompanyDTO companyDTO);

    @Mapping(target = "tipoDoc", source = "tipoDocumento")
    @Mapping(target = "numDoc", source = "numDocumento")
    @Mapping(target = "rznSocial", source = "razonSocial")
    LycetDestinatario toLycetDestinatario(ReceiverDTO receiverDTO);

    @Mapping(target = "codTraslado", source = "codTraslado")
    @Mapping(target = "modTraslado", source = "modTraslado")
    @Mapping(target = "pesoTotal", ignore = true) // Se calcula automáticamente después del mapeo
    @Mapping(target = "undPesoTotal", source = "unidadPeso")
    @Mapping(target = "fecTraslado", source = "fecTraslado", qualifiedByName = "localDateToLocalDateTime")
    @Mapping(target = "llegada", source = "llegada")
    @Mapping(target = "partida", source = "partida")
    LycetEnvio toLycetEnvio(ShipmentDTO shipmentDTO);

    @Mapping(target = "placa", source = "principal.numPlaca")
    @Mapping(target = "secundarios", source = "secundarios")
    LycetVehiculo toLycetVehiculo(VehiclesDTO vehiclesDTO);

    @Mapping(target = "placa", source = "numPlaca")
    LycetVehiculoSecundario toLycetVehiculoSecundario(
            VehicleDTO vehicleDTO);

    @Mapping(target = "tipo", constant = "Principal")
    @Mapping(target = "tipoDoc", source = "typeDocument")
    @Mapping(target = "nroDoc", source = "numDocument")
    @Mapping(target = "licencia", source = "numLicencia")
    @Mapping(target = "nombres", source = "nombres")
    @Mapping(target = "apellidos", source = "apellidos")
    LycetChofer toLycetChofer(DriverDTO driverDTO);

    @Mapping(target = "ubigueo", source = "codUbigeo")
    @Mapping(target = "direccion", source = "direccion")
    LycetDireccion toLycetDireccion(DireccionDTO direccionDTO);

    @Mapping(target = "codigo", source = "codProducto")
    @Mapping(target = "descripcion", source = "descProducto")
    @Mapping(target = "unidad", source = "unidadMedida")
    @Mapping(target = "cantidad", source = "cantidad")
    LycetDetail toLycetDetail(DetailShipmentDTO detailShipmentDTO);

    /**
     * Convierte LocalDate a LocalDateTime con hora 00:00:00.
     */
    @Named("localDateToLocalDateTime")
    default LocalDateTime localDateToLocalDateTime(LocalDate localDate) {
        if (localDate == null) {
            return LocalDateTime.now();
        }
        return localDate.atStartOfDay();
    }

    /**
     * Convierte un conductor a lista de choferes.
     */
    default List<LycetChofer> conductorToList(DriverDTO conductor) {
        if (conductor == null) {
            return List.of();
        }
        return List.of(toLycetChofer(conductor));
    }

    /**
     * Convierte lista de vehículos secundarios.
     */
    default List<LycetVehiculoSecundario> vehiculosSecundariosToList(
            List<VehicleDTO> vehiculos) {
        if (vehiculos == null) {
            return List.of();
        }
        return vehiculos.stream()
                .map(this::toLycetVehiculoSecundario)
                .collect(Collectors.toList());
    }

    /**
     * Calcula automáticamente el peso total sumando los pesos de los detalles
     * después del mapeo de LycetGuiaRemisionRequest.
     */
    @AfterMapping
    default void calcularPesoTotalAutomatico(@MappingTarget LycetGuiaRemisionRequest target,
            ReferralGuideDTO source) {
        if (target.getEnvio() != null && source.getDetalle() != null && !source.getDetalle().isEmpty()) {
            double pesoTotal = source.getDetalle().stream()
                    .mapToDouble(detalle -> detalle.getPeso() != null ? detalle.getPeso() : 0.0)
                    .sum();
            target.getEnvio().setPesoTotal(pesoTotal);
        }
    }

    /**
     * Calcula automáticamente el peso total sumando los pesos de los detalles
     * después del mapeo de LycetEnvio.
     */
    @AfterMapping
    default void calcularPesoTotalEnvio(@MappingTarget LycetEnvio target,
            ShipmentDTO source) {
        // Si el pesoTotal ya está establecido en el source, lo mantenemos
        if (source.getPesoTotal() != null) {
            target.setPesoTotal(source.getPesoTotal());
        }
        // Si no, se calcula automáticamente en calcularPesoTotalAutomatico
    }
}