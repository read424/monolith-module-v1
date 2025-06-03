package integration.com.walrex.module_almacen.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

@TestConfiguration
public class R2dbcTestConfig {

    @Bean
    @Primary
    public R2dbcEntityTemplate r2dbcEntityTemplate(ConnectionFactory connectionFactory) {
        return new R2dbcEntityTemplate(connectionFactory);
    }

    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }


    // Si necesitas mockear el repositorio:
    /*
    @Bean
    public OrdenIngresoEntityMapper ordenIngresoEntityMapper() {
        return Mappers.getMapper(OrdenIngresoEntityMapper.class);
    }

    @Bean
    @Primary
    public OrdenIngresoRepository ordenIngresoRepository() {
        // Implementación de mock o real según necesites
    }
     */
}
