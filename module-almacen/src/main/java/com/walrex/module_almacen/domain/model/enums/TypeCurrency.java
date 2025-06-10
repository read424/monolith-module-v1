package com.walrex.module_almacen.domain.model.enums;

import lombok.Getter;

@Getter
public enum TypeCurrency {
    DOLLAR(2);

    private final int id;

    TypeCurrency(int id){ this.id = id; }

    public static TypeCurrency fromId(int id){
        for(TypeCurrency currency: values()){
            if(currency.id==id){
                return currency;
            }
        }
        throw new IllegalArgumentException("Currency no válido: "+ id);
    }
}
