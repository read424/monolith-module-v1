package com.walrex.module_almacen.domain.model.enums;

import lombok.Getter;

@Getter
public enum TypeMotivo {
    TRANSFORMACION(20, "TRANSFORMACION", 14);

    private final int id;
    private final String descMotivo;
    private final int idRelacionMotivo;

    TypeMotivo(int id, String descMotivo, int idRelacionMotivo){
        this.id = id;
        this.descMotivo= descMotivo;
        this.idRelacionMotivo= idRelacionMotivo;
    }

    public static TypeMotivo fromId(int id){
        for(TypeMotivo motivo: values()){
            if(motivo.id == id){
                return motivo;
            }
        }
        throw new IllegalArgumentException("Motivo no válido: " + id);
    }
}
