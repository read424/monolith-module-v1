package com.walrex.module_articulos.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArticuloSearchCriteriaTest {

    @Test
    void debeCrearCriteriosDeBusquedaValidos(){
        // Given
        String queryText = "CALLTOP";
        int page = 0;
        int size = 10;

        // When
        ArticuloSearchCriteria criteria = ArticuloSearchCriteria.builder()
                .search(queryText)
                .page(page)
                .size(size)
                .build();

        // Then
        assertEquals(queryText, criteria.getSearch());
        assertEquals(page, criteria.getPage());
        assertEquals(size, criteria.getSize());
    }
}
