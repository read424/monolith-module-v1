package com.walrex.module_almacen;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.*;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

/**
 * 🏗️ Test Arquitectónico - Arquitectura Hexagonal (Ports & Adapters)
 * 
 * Valida automáticamente que se respeten las reglas de:
 * - Clean Code
 * - Principios SOLID
 * - Arquitectura Hexagonal
 * - Patrones de Diseño
 */
@DisplayName("🏛️ Hexagonal Architecture Tests")
class HexagonalArchitectureTest {

    private JavaClasses importedClasses;

    @BeforeEach
    void setUp() {
        importedClasses = new ClassFileImporter()
                .importPackages("com.walrex.module_almacen");
    }

    // 🏛️ LAYER ARCHITECTURE RULES

    @Test
    @DisplayName("🎯 Domain layer should not depend on application or infrastructure")
    void domainShouldNotDependOnApplicationOrInfrastructure() {
        ArchRule domainRule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..application..", "..infrastructure..")
                .because("Domain should not depend on application or infrastructure layers");

        domainRule.check(importedClasses);
    }

    @Test
    @DisplayName("🚪 Ports should only be interfaces")
    void portsShouldOnlyBeInterfaces() {
        ArchRule rule = classes()
                .that().resideInAnyPackage("..ports.input..", "..ports.output..")
                .should().beInterfaces()
                .because("Ports define contracts and should be interfaces");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("🔧 Adapters should implement ports")
    void adaptersShouldImplementPorts() {
        ArchRule rule = classes()
                .that().resideInAnyPackage("..adapters..")
                .and().areNotInterfaces()
                .should().implementInterface()
                .because("Adapters should implement port interfaces");

        // Note: This is a simplified check, you might need to customize based on your
        // specific naming
        rule.check(importedClasses);
    }

    // 🧹 CLEAN CODE RULES

    @Test
    @DisplayName("📏 Classes should not exceed 200 lines")
    void classesShouldNotExceed200Lines() {
        ArchRule rule = classes()
                .should().haveSourceCodeLineLessThan(200)
                .because("Large classes violate Single Responsibility Principle");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("🎯 Use Cases should have descriptive names")
    void useCasesShouldHaveDescriptiveNames() {
        ArchRule rule = classes()
                .that().resideInAPackage("..ports.input..")
                .should().haveSimpleNameEndingWith("UseCase")
                .because("Use Cases should follow naming convention");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("🏪 Repositories should be ports, not implementations")
    void repositoriesShouldBePorts() {
        ArchRule rule = classes()
                .that().haveSimpleNameContaining("Repository")
                .and().resideInAPackage("..domain..")
                .should().beInterfaces()
                .because("Repository in domain should be interfaces (ports)");

        rule.check(importedClasses);
    }

    // ⚡ SOLID PRINCIPLES

    @Test
    @DisplayName("🎯 Services should only have one public constructor")
    void servicesShouldHaveOnlyOnePublicConstructor() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Service")
                .should().haveOnlyFinalFields()
                .andShould().haveOnlyPrivateConstructors().orShould().haveExactlyOnePublicConstructor()
                .because("Services should follow Dependency Inversion Principle");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("🏭 Factories should be in application layer")
    void factoriesShouldBeInApplicationLayer() {
        ArchRule rule = classes()
                .that().haveSimpleNameContaining("Factory")
                .should().resideInAnyPackage("..application..")
                .because("Factories coordinate object creation and belong to application layer");

        rule.check(importedClasses);
    }

    // 🎨 DESIGN PATTERNS

    @Test
    @DisplayName("📋 DTOs should be in specific packages")
    void dtosShouldBeInSpecificPackages() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("DTO")
                .or().haveSimpleNameEndingWith("Request")
                .or().haveSimpleNameEndingWith("Response")
                .should().resideInAnyPackage("..dto..", "..request..", "..response..")
                .because("DTOs should be organized in specific packages");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("🔄 Mappers should not be in domain layer")
    void mappersShouldNotBeInDomainLayer() {
        ArchRule rule = classes()
                .that().haveSimpleNameContaining("Mapper")
                .should().notResideInAPackage("..domain..")
                .because("Mappers handle data transformation and don't belong in domain");

        rule.check(importedClasses);
    }

    // 🚫 ANTI-PATTERNS

    @Test
    @DisplayName("🚫 No God classes allowed")
    void noGodClassesAllowed() {
        ArchRule rule = classes()
                .should().notHaveModifier(java.lang.reflect.Modifier.PUBLIC)
                .orShould().haveSourceCodeLineLessThan(200)
                .because("God classes violate Single Responsibility Principle");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("🔍 Domain entities should not depend on Spring")
    void domainEntitiesShouldNotDependOnSpring() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
                .because("Domain should be framework-independent");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("🏛️ Infrastructure should not leak to domain")
    void infrastructureShouldNotLeakToDomain() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..")
                .because("Domain should not depend on infrastructure details");

        rule.check(importedClasses);
    }

    // 📋 SPRING ANNOTATIONS

    @Test
    @DisplayName("🎯 @Service should only be in domain services")
    void serviceAnnotationShouldOnlyBeInDomainServices() {
        ArchRule rule = classes()
                .that().areAnnotatedWith("org.springframework.stereotype.Service")
                .should().resideInAPackage("..domain.service..")
                .because("@Service annotation should only be used in domain services");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("🎯 @Component should only be in infrastructure")
    void componentAnnotationShouldOnlyBeInInfrastructure() {
        ArchRule rule = classes()
                .that().areAnnotatedWith("org.springframework.stereotype.Component")
                .should().resideInAPackage("..infrastructure..")
                .because("@Component should only be used in infrastructure adapters");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("🚫 @RestController should NOT be used (WebFlux reactive)")
    void restControllerShouldNotBeUsed() {
        ArchRule rule = noClasses()
                .should().beAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .because("WebFlux reactive should use RouterFunction + Handler instead of @RestController");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("🔄 Handlers should be in inbound adapters")
    void handlersShouldBeInInboundAdapters() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Handler")
                .should().resideInAPackage("..infrastructure.adapters.inbound..")
                .because("Reactive handlers are inbound adapters");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("🛣️ Routers should be in inbound adapters")
    void routersShouldBeInInboundAdapters() {
        ArchRule rule = classes()
                .that().haveSimpleNameContaining("Router")
                .should().resideInAPackage("..infrastructure.adapters.inbound..")
                .because("Reactive routers are inbound adapters");

        rule.check(importedClasses);
    }

    // 🧪 TESTING RULES

    @Test
    @DisplayName("🧪 Test classes should follow naming convention")
    void testClassesShouldFollowNamingConvention() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Test")
                .should().resideInAnyPackage("..test..")
                .because("Test classes should be in test packages");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("🔬 Unit tests should test domain logic")
    void unitTestsShouldTestDomainLogic() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Test")
                .and().resideInAPackage("..domain..")
                .should().onlyAccessClassesThat()
                .resideInAnyPackage("..domain..", "java..", "org.junit..", "org.assertj..")
                .because("Unit tests should focus on domain logic without external dependencies");

        rule.check(importedClasses);
    }
}