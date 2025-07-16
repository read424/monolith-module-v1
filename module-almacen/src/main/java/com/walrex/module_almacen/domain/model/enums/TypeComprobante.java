package com.walrex.module_almacen.domain.model.enums;

import lombok.Getter;

@Getter
public enum TypeComprobante {
    GUIA_REMISION_REMITENTE_SUNAT(5, 7),
    GUIA_REMISION_REMITENTE(5, 8);

    private final int id_comprobante;
    private final int id_serie;

    TypeComprobante(int id_comprobante, int id_serie) {
        this.id_comprobante = id_comprobante;
        this.id_serie = id_serie;
    }

    public static TypeComprobante fromId(int id) {
        for (TypeComprobante comprobante : values()) {
            if (comprobante.id_comprobante == id) {
                return comprobante;
            }
        }
        throw new IllegalArgumentException("Comprobante no válido: " + id);
    }
}
