package integration.com.walrex.module_almacen;

import integration.com.walrex.module_almacen.config.AbstractPostgreSQLContainerTest;

import java.util.logging.Level;
import java.util.logging.Logger;

//@SpringBootTest
//@ActiveProfiles("test-almacenes")
public class MinimalContextTest extends AbstractPostgreSQLContainerTest {
    private static final Logger logger = Logger.getLogger(MinimalContextTest.class.getName());

    //@BeforeAll
    static void setup() {
        // Configura un logger para la clase actual
        logger.setLevel(Level.ALL);

        // Configura un handler de excepciones no capturadas
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logger.severe("Uncaught exception in thread " + thread.getName() + ": " + throwable.getMessage());
            throwable.printStackTrace();
        });

        // Log información del contenedor
        try {
            logger.info("PostgreSQL container running: " + getPostgreSQLContainer().isRunning());
            logger.info("PostgreSQL container URL: " + getPostgreSQLContainer().getJdbcUrl());
        } catch (Exception e) {
            logger.severe("Error accessing PostgreSQL container: " + e.getMessage());
            e.printStackTrace();
        }
    }


    //@Test
    void contextLoads() {
        // Esta prueba fallará si el contexto no se puede cargar
        System.out.println("Context loaded successfully!");
    }
}
