package com.walrex.module_almacen.domain.model.enums;

public enum TypeUnidadMedida {
    KILO(1),
    GRAMOS(6);

    private final int id;

    TypeUnidadMedida(int id){ this.id = id; }

    public int getId(){ return id; }

    public static TypeUnidadMedida fromId(int id){
        for(TypeUnidadMedida unidadmedida: values()){
            if(unidadmedida.id==id){
                return unidadmedida;
            }
        }
        throw  new IllegalArgumentException("Tipo Movimiento no válido " + id);
    }
}
