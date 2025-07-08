package com.walrex.module_almacen.domain.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.walrex.module_almacen.application.ports.input.RegistrarDevolucionRollosUseCase;
import com.walrex.module_almacen.application.ports.output.DevolucionRollosPort;
import com.walrex.module_almacen.domain.model.dto.RolloDevolucionDTO;
import com.walrex.module_almacen.domain.model.dto.SalidaDevolucionDTO;
import com.walrex.module_almacen.domain.model.exceptions.StockInsuficienteException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio de dominio para registrar devolución de rollos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrarDevolucionRollosService implements RegistrarDevolucionRollosUseCase {

    private final DevolucionRollosPort devolucionRollosPort;

    @Override
    @Transactional
    public Mono<SalidaDevolucionDTO> registrarDevolucionRollos(SalidaDevolucionDTO salidaDevolucion,
            Integer idUsuario) {
        log.info("🔄 Iniciando registro de devolución de rollos - Cliente: {}, Motivo: {}, Artículos: {}",
                salidaDevolucion.getIdCliente(), salidaDevolucion.getIdMotivo(),
                salidaDevolucion.getArticulos().size());

        return aplicarReglasDeNegocio(salidaDevolucion)
                .doOnNext(dto -> asignarMetadatos(dto, idUsuario))
                .flatMap(this::procesarDevolucion)
                .doOnNext(resultado -> log.info(
                        "✅ Devolución registrada exitosamente - ID: {}, Código: {}, Total artículos: {}",
                        resultado.getIdOrdenSalida(), resultado.getCodSalida(), resultado.getArticulos().size()))
                .doOnError(error -> log.error("❌ Error al registrar devolución: {}", error.getMessage()));
    }

    /**
     * Aplica las reglas de negocio del dominio
     */
    private Mono<SalidaDevolucionDTO> aplicarReglasDeNegocio(SalidaDevolucionDTO salidaDevolucion) {
        log.debug("🔍 Aplicando reglas de negocio para devolución");

        return Mono.just(salidaDevolucion)
                .flatMap(this::validarRollosSeleccionadosNoDevueltos)
                .flatMap(this::validarPesosPositivos)
                .doOnNext(dto -> log.debug("✅ Reglas de negocio aplicadas correctamente"));
    }

    /**
     * Valida que los rollos seleccionados no hayan sido devueltos anteriormente
     * REGLA DE NEGOCIO: Un rollo solo puede ser devuelto una vez
     */
    private Mono<SalidaDevolucionDTO> validarRollosSeleccionadosNoDevueltos(SalidaDevolucionDTO salidaDevolucion) {
        log.debug("🔍 Validando que los rollos seleccionados no hayan sido devueltos anteriormente");

        return Flux.fromIterable(salidaDevolucion.getArticulos())
                .filter(articulo -> articulo.getRollos() != null && !articulo.getRollos().isEmpty())
                .flatMap(articulo -> Flux.fromIterable(articulo.getRollos())
                        .filter(rollo -> rollo.getSelected() != null && rollo.getSelected())
                        .filter(rollo -> rollo.getDelete() == null || !rollo.getDelete())
                        .flatMap(rollo -> devolucionRollosPort
                                .verificarRolloYaDevuelto(rollo.getIdDetOrdenIngresoPeso())
                                .filter(Boolean::booleanValue)
                                .flatMap(yaDevuelto -> Mono.<RolloDevolucionDTO>error(new StockInsuficienteException(
                                        "El rollo " + rollo.getCodRollo() + " ya fue devuelto anteriormente")))
                                .switchIfEmpty(Mono.just(rollo))))
                .then(Mono.just(salidaDevolucion));
    }

    /**
     * Valida que los pesos de los rollos seleccionados sean positivos
     * REGLA DE NEGOCIO: Los pesos deben ser valores positivos
     */
    private Mono<SalidaDevolucionDTO> validarPesosPositivos(SalidaDevolucionDTO salidaDevolucion) {
        log.debug("🔍 Validando que los pesos de los rollos seleccionados sean positivos");

        return Flux.fromIterable(salidaDevolucion.getArticulos())
                .filter(articulo -> articulo.getRollos() != null && !articulo.getRollos().isEmpty())
                .flatMap(articulo -> {
                    // Obtener rollos seleccionados
                    var rollosSeleccionados = articulo.getRollos().stream()
                            .filter(rollo -> rollo.getSelected() != null && rollo.getSelected())
                            .toList();

                    // Si no hay rollos seleccionados, omitir artículo
                    if (rollosSeleccionados.isEmpty()) {
                        log.debug("🔄 Artículo {} omitido - sin rollos seleccionados", articulo.getIdArticulo());
                        return Mono.just(articulo);
                    }

                    // Validar pesos positivos
                    return Flux.fromIterable(rollosSeleccionados)
                            .flatMap(rollo -> {
                                if (rollo.getPesoRollo() == null
                                        || rollo.getPesoRollo().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                                    return Mono.<RolloDevolucionDTO>error(new IllegalArgumentException(
                                            "El peso del rollo " + rollo.getCodRollo() +
                                                    " debe ser mayor a 0 para el artículo "
                                                    + articulo.getIdArticulo()));
                                }
                                return Mono.just(rollo);
                            })
                            .then(Mono.just(articulo));
                })
                .then(Mono.just(salidaDevolucion));
    }

    /**
     * Asigna metadatos necesarios para el procesamiento
     */
    private void asignarMetadatos(SalidaDevolucionDTO salidaDevolucion, Integer idUsuario) {
        // Asignar usuario
        salidaDevolucion.setIdUsuario(idUsuario);
        salidaDevolucion.setIdAlmacenOrigen(8);
        salidaDevolucion.setIdAlmacenDestino(100);

        // Asignar fecha de registro si no está presente
        if (salidaDevolucion.getFechaRegistro() == null) {
            salidaDevolucion.setFechaRegistro(LocalDate.now());
        }

        log.debug("🔄 Metadatos asignados - Usuario: {}, Fecha: {}",
                idUsuario, salidaDevolucion.getFechaRegistro());
    }

    /**
     * Procesa la devolución a través del puerto de salida
     */
    private Mono<SalidaDevolucionDTO> procesarDevolucion(SalidaDevolucionDTO salidaDevolucion) {
        int totalRollos = salidaDevolucion.getArticulos().stream()
                .mapToInt(articulo -> (int) articulo.getRollos().stream()
                        .filter(rollo -> rollo.getSelected() && !rollo.getDelete())
                        .count())
                .sum();

        log.debug("🔄 Procesando devolución - {} artículos, {} rollos total",
                salidaDevolucion.getArticulos().size(), totalRollos);

        return devolucionRollosPort.registrarDevolucionRollos(salidaDevolucion)
                .doOnSuccess(resultado -> log.info(
                        "✅ Devolución procesada exitosamente - Total artículos: {}, Total rollos: {}",
                        resultado.getArticulos().size(), totalRollos));
    }
}