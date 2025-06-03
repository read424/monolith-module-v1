package com.walrex.module_almacen.domain.model.enums;

public enum TypeMovimiento {
    INTERNO_TRANSFORMACION(3),
    APROBACION_SALIDA_REQUERIMIENTO(2);

    private final int id;

    TypeMovimiento(int id){ this.id = id;}

    public int getId(){return id;}

    public static TypeMovimiento fromId(int id){
        for(TypeMovimiento movimiento: values()){
            if(movimiento.id==id){
                return movimiento;
            }
        }
        throw  new IllegalArgumentException("Tipo Movimiento no válido " + id);
    }
}
