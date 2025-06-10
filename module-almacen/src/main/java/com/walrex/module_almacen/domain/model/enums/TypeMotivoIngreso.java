package com.walrex.module_almacen.domain.model.enums;

import lombok.Getter;

@Getter
public enum TypeMotivoIngreso {
    TRANSFORMACION(14, "TRANSFORMACION");

    private final int id;
    private final String descMotivo;

    TypeMotivoIngreso(int id, String descMotivo){
        this.id = id;
        this.descMotivo= descMotivo;
    }

    public static TypeMotivoIngreso fromId(int id){
        for(TypeMotivoIngreso motivo: values()){
            if(motivo.id == id){
                return motivo;
            }
        }
        throw new IllegalArgumentException("Motivo no válido: " + id);
    }
}
