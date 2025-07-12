package com.walrex.module_ecomprobantes.infrastructure.adapters.config.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class ComprobantesKafkaProperties {

    private Kafka kafka = new Kafka();
    private Comprobante comprobante = new Comprobante();

    @Data
    public static class Kafka {
        private Consumer consumer = new Consumer();
        private Producer producer = new Producer();

        @Data
        public static class Consumer {
            private Backpressure backpressure = new Backpressure();
            private Processing processing = new Processing();

            @Data
            public static class Backpressure {
                private Integer bufferSize = 1000;
                private String overflowStrategy = "BUFFER";
                private Integer prefetch = 50;
            }

            @Data
            public static class Processing {
                private Integer parallelism = 10;
                private Integer maxConcurrency = 20;
            }
        }

        @Data
        public static class Producer {
            private Backpressure backpressure = new Backpressure();
            private Retry retry = new Retry();

            @Data
            public static class Backpressure {
                private Integer bufferSize = 500;
                private String overflowStrategy = "DROP_LATEST";
            }

            @Data
            public static class Retry {
                private Integer maxAttempts = 3;
                private Duration backoffDelay = Duration.ofMillis(1000);
            }
        }
    }

    @Data
    public static class Comprobante {
        private Processing processing = new Processing();

        @Data
        public static class Processing {
            private Duration timeout = Duration.ofSeconds(45);
            private Integer maxRetries = 3;
            private Integer batchSize = 20;
        }
    }
}