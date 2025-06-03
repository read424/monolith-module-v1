package com.walrex.module_almacen.domain.model.enums;

public enum TypeAlmacen {
    INSUMOS(1),
    TELA_CRUDA(2);

    private final int id;

    TypeAlmacen(int id){
        this.id=id;
    }

    public int getId(){
        return id;
    }

    public static TypeAlmacen fromId(int id) {
        for (TypeAlmacen almacen : values()) {
            if (almacen.id == id) {
                return almacen;
            }
        }
        throw new IllegalArgumentException("Almacén no válido: " + id);
    }
}
