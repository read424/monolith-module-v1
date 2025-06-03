package integration.com.walrex.module_almacen.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public class AbstractKafkaTestContainer {

    // Red compartida para que los contenedores puedan comunicarse
    static final Network network = Network.newNetwork();

    // Kafka usando la imagen de Bitnami
    protected static final GenericContainer<?> kafkaContainer = new GenericContainer<>(
            DockerImageName.parse("bitnami/kafka:latest"))
            .withNetwork(network)
            .withNetworkAliases("kafka")
            .withExposedPorts(9092)
            .withEnv("KAFKA_CFG_NODE_ID", "1")
            .withEnv("KAFKA_CFG_PROCESS_ROLES", "broker,controller")
            .withEnv("KAFKA_CFG_CONTROLLER_QUORUM_VOTERS", "1@kafka:9093")
            .withEnv("KAFKA_CFG_LISTENERS", "PLAINTEXT://:9092,CONTROLLER://:9093")
            .withEnv("KAFKA_CFG_ADVERTISED_LISTENERS", "PLAINTEXT://kafka:9092")
            .withEnv("KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP", "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT")
            .withEnv("KAFKA_CFG_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
            .withEnv("KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE", "true")
            .withEnv("ALLOW_PLAINTEXT_LISTENER", "yes")
            .withEnv("KAFKA_KRAFT_CLUSTER_ID", "abcdefghijklmnopqrstuv")
            .waitingFor(Wait.forLogMessage(".*Kafka Server started.*", 1))
            .withStartupTimeout(Duration.ofMinutes(5));

    // Schema Registry
    protected static final GenericContainer<?> schemaRegistryContainer = new GenericContainer<>(
            DockerImageName.parse("confluentinc/cp-schema-registry:latest"))
            .withNetwork(network)
            .withNetworkAliases("schema-registry")
            .withExposedPorts(8081)
            .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
            .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
            .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "kafka:9092")
            .dependsOn(kafkaContainer)
            .waitingFor(Wait.forHttp("/subjects").forStatusCode(200))
            .withStartupTimeout(Duration.ofMinutes(2));

    // Kafka REST Proxy
    protected static final GenericContainer<?> kafkaRestContainer = new GenericContainer<>(
            DockerImageName.parse("confluentinc/cp-kafka-rest:6.0.0"))
            .withNetwork(network)
            .withNetworkAliases("kafka-rest")
            .withExposedPorts(8082)
            .withEnv("KAFKA_REST_HOST_NAME", "kafka-rest")
            .withEnv("KAFKA_REST_LISTENERS", "http://0.0.0.0:8082")
            .withEnv("KAFKA_REST_BOOTSTRAP_SERVERS", "kafka:9092")
            .withEnv("KAFKA_REST_SCHEMA_REGISTRY_URL", "http://schema-registry:8081")
            .dependsOn(kafkaContainer, schemaRegistryContainer)
            .waitingFor(Wait.forHttp("/topics").forStatusCode(200))
            .withStartupTimeout(Duration.ofMinutes(2));

    static {
        try {
            // Iniciar contenedores
            kafkaContainer.start();
            schemaRegistryContainer.start();
            kafkaRestContainer.start();

            // Log info de conexión
            System.out.println("🚀 Test Infrastructure Started:");
            System.out.println("Kafka: " + kafkaContainer.getHost() + ":" + kafkaContainer.getMappedPort(9092));
            System.out.println("Schema Registry: http://" + schemaRegistryContainer.getHost() + ":" +
                    schemaRegistryContainer.getMappedPort(8081));
            System.out.println("Kafka REST: http://" + kafkaRestContainer.getHost() + ":" +
                    kafkaRestContainer.getMappedPort(8082));
        } catch (Exception e) {
            System.err.println("Error starting containers: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to start containers", e);
        }
    }

    /**
     * Proporciona dinámicamente las propiedades de configuración a Spring.
     * Estas sobreescribirán cualquier configuración existente.
     */
    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        String bootstrapServers = kafkaContainer.getHost() + ":" + kafkaContainer.getMappedPort(9092);
        String schemaRegistryUrl = "http://" + schemaRegistryContainer.getHost() + ":" +
                schemaRegistryContainer.getMappedPort(8081);
        String kafkaRestUrl = "http://" + kafkaRestContainer.getHost() + ":" +
                kafkaRestContainer.getMappedPort(8082);

        // Propiedades básicas de Kafka
        registry.add("spring.kafka.bootstrap-servers", () -> bootstrapServers);

        // Propiedades para Schema Registry
        registry.add("spring.kafka.properties.schema.registry.url", () -> schemaRegistryUrl);
        registry.add("spring.kafka.producer.properties.schema.registry.url", () -> schemaRegistryUrl);
        registry.add("spring.kafka.consumer.properties.schema.registry.url", () -> schemaRegistryUrl);

        // Configuraciones para consumidor de prueba
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.consumer.properties.specific.avro.reader", () -> "true");

        // URL de Kafka REST (si la usas)
        registry.add("kafka.rest.url", () -> kafkaRestUrl);
    }

}
