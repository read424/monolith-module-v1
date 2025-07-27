package com.walrex.module_ecomprobantes.infrastructure.adapters.outbound.mapper;

import java.util.List;

import org.mapstruct.*;

import com.walrex.module_ecomprobantes.domain.model.dto.*;

/**
 * Mapper para convertir proyecciones de guía de remisión a DTOs de respuesta.
 * Utiliza MapStruct para generar automáticamente el código de mapeo.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GuiaRemisionProjectionMapper {

    /**
     * Convierte la proyección de cabecera a ReferralGuideDTO.
     */
    @Mapping(target = "idVersion", constant = "2022")
    @Mapping(target = "typeGuide", constant = "nuCompro")
    @Mapping(target = "codSerie", source = "nuSerie")
    @Mapping(target = "numCorrelativo", source = "nroComprobante")
    @Mapping(target = "fecEmision", source = "feEmision")
    @Mapping(target = "company", source = ".", qualifiedByName = "toCompany")
    @Mapping(target = "receiver", source = ".", qualifiedByName = "toReceiver")
    @Mapping(target = "shipment", source = ".")
    @Mapping(target = "detalle", source = "detalles")
    ReferralGuideDTO toReferralGuideDTO(GuiaRemisionCabeceraProjection projection);

    /**
     * Convierte la proyección de cabecera a CompanyDTO.
     */
    @Mapping(source = "numDocumento", target = "numDocumento")
    @Mapping(source = "razonSocial", target = "razonSocial")
    @Mapping(target = "nombreComercial", ignore = true) // No disponible en la proyección
    @Mapping(source = "companyDireccion", target = "direccion")
    @Mapping(source = "tlfPrincipal", target = "telefono")
    @Mapping(source = "numFax", target = "tlf_fax")
    @Mapping(source = "tlfSecundario", target = "tlf_movil")
    @Mapping(target = "siteWeb", ignore = true)
    @Mapping(target = "email", ignore = true)
    CompanyDTO toCompanyDTO(GuiaRemisionCabeceraProjection projection);

    /**
     * Convierte la proyección de cabecera a ReceiverDTO.
     */
    @Mapping(source = "codTipodoc", target = "codTipodoc")
    @Mapping(source = "abrevDoc", target = "tipoDocumento")
    @Mapping(source = "nuRuc", target = "numDocumento")
    @Mapping(source = "rznSocial", target = "razonSocial")
    @Mapping(source = "direccion", target = "direccion.direccion")
    ReceiverDTO toReceiverDTO(GuiaRemisionCabeceraProjection projection);

    /**
     * Convierte la proyección de cabecera a ShipmentDTO.
     */
    @Mapping(target = "codTraslado", source = "codMotivoTraslado")
    @Mapping(target = "descTraslado", source = "descMotivoTraslado")
    @Mapping(target = "modTraslado", source = "codModTraslado")
    @Mapping(target = "pesoTotal", ignore = true)
    @Mapping(target = "unidadPeso", constant = "KGM")
    @Mapping(target = "fecTraslado", source = "feEmision")
    @Mapping(target = "transportista", source = ".")
    @Mapping(target = "vehiculos", source = ".", qualifiedByName = "toVehiculo")
    @Mapping(target = "conductor", source = ".", qualifiedByName = "toConductor")
    @Mapping(target = "llegada", source = ".", qualifiedByName = "toDireccionLlegada")
    @Mapping(target = "partida", source = ".", qualifiedByName = "toDireccionPartida")
    ShipmentDTO toShipmentDTO(GuiaRemisionCabeceraProjection projection);

    /**
     * Convierte la proyección de cabecera a CarrierDTO.
     */
    @Mapping(source = "docTranspCodTipodoc", target = "typeDocument")
    @Mapping(source = "abrevDocTransp", target = "descTypeDocument")
    @Mapping(source = "transpNumTipoDocumento", target = "numDocument")
    @Mapping(source = "transpRazonSocial", target = "razonSocial")
    @Mapping(target = "nroMTC", ignore = true) // No disponible en la proyección
    CarrierDTO toCarrierDTO(GuiaRemisionCabeceraProjection projection);

    /**
     * Convierte la proyección de cabecera a DireccionDTO para llegada.
     */
    @Mapping(target = "codUbigeo", source = "ubigeoLlegada")
    @Mapping(target = "direccion", source = "direcLlegada")
    DireccionDTO toDireccionLlegadaDTO(GuiaRemisionCabeceraProjection projection);

    /**
     * Convierte la proyección de cabecera a DireccionDTO para partida.
     */
    @Mapping(target = "codUbigeo", source = "ubigeoPartida")
    @Mapping(target = "direccion", source = "direcPartida")
    DireccionDTO toDireccionPartidaDTO(GuiaRemisionCabeceraProjection projection);

    /**
     * Método cualificado para mapear a dirección de llegada.
     */
    @Named("toDireccionLlegada")
    default DireccionDTO toDireccionLlegada(GuiaRemisionCabeceraProjection projection) {
        return toDireccionLlegadaDTO(projection);
    }

    /**
     * Método cualificado para mapear a dirección de partida.
     */
    @Named("toDireccionPartida")
    default DireccionDTO toDireccionPartida(GuiaRemisionCabeceraProjection projection) {
        return toDireccionPartidaDTO(projection);
    }

    /**
     * Método cualificado para mapear a CompanyDTO.
     */
    @Named("toCompany")
    default CompanyDTO toCompany(GuiaRemisionCabeceraProjection projection) {
        return toCompanyDTO(projection);
    }

    /**
     * Método cualificado para mapear a ReceiverDTO.
     */
    @Named("toReceiver")
    default ReceiverDTO toReceiver(GuiaRemisionCabeceraProjection projection) {
        return toReceiverDTO(projection);
    }

    /**
     * Método cualificado para mapear a VehiculoDTO.
     */
    @Named("toVehiculo")
    default VehiclesDTO toVehiculo(GuiaRemisionCabeceraProjection projection) {
        return toVehiculoDTO(projection);
    }

    /**
     * Convierte la proyección de cabecera a VehiculoDTO.
     */
    @Mapping(target = "principal.numPlaca", source = "numPlaca")
    VehiclesDTO toVehiculoDTO(GuiaRemisionCabeceraProjection projection);

    /**
     * Método cualificado para mapear a ConductorDTO.
     */
    @Named("toConductor")
    default DriverDTO toConductor(GuiaRemisionCabeceraProjection projection) {
        return toConductorDTO(projection);
    }

    /**
     * Convierte la proyección de cabecera a ConductorDTO.
     */
    @Mapping(target = "typeDriver", constant = "Principal")
    @Mapping(source = "codTipodocConductor", target = "typeDocument")
    @Mapping(source = "tipoDocConductor", target = "descTypeDocument")
    @Mapping(source = "abrevDocConductor", target = "abrevTypeDocument")
    @Mapping(source = "numDocumentoConductor", target = "numDocument")
    @Mapping(source = "nombresConductor", target = "nombres")
    @Mapping(source = "apellidosConductor", target = "apellidos")
    @Mapping(source = "numLicenciaConductor", target = "numLicencia")
    DriverDTO toConductorDTO(GuiaRemisionCabeceraProjection projection);

    /**
     * Convierte la lista de detalles a DetailShipmentDTO.
     */
    @Mapping(target = "codProducto", source = "codArticulo")
    @Mapping(target = "descProducto", source = "descArticulo")
    @Mapping(target = "unidadMedida", source = "codUnidad")
    @Mapping(target = "cantidad", constant = "1") // Valor por defecto
    @Mapping(target = "peso", source = "peso")
    DetailShipmentDTO toDetailShipmentDTO(GuiaRemisionDetalleProjection detalle);

    /**
     * Convierte la lista de detalles.
     */
    List<DetailShipmentDTO> toDetailShipmentDTOList(List<GuiaRemisionDetalleProjection> detalles);

    /**
     * Calcula el peso total sumando todos los pesos de los detalles.
     */
    @Named("calculatePesoTotal")
    default Double calculatePesoTotal(List<GuiaRemisionDetalleProjection> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            return 0.0;
        }
        return detalles.stream()
                .mapToDouble(detalle -> detalle.getPeso() != null ? detalle.getPeso() : 0.0)
                .sum();
    }
}