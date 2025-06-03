package com.walrex.module_almacen.common.kafka;

import com.walrex.module_almacen.infrastructure.adapters.inbound.consumer.OrdenAjusteInventarioKafkaConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaInitializer implements ApplicationListener<ApplicationReadyEvent> {
    private final OrdenAjusteInventarioKafkaConsumer ordenAjusteConsumer;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // Iniciar consumidores solo cuando la aplicación esté completamente lista
        ordenAjusteConsumer.startListening();
    }
}
