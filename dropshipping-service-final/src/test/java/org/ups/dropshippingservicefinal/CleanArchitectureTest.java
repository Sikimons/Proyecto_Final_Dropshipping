package org.ups.dropshippingservicefinal;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class CleanArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("org.ups.dropshippingservicefinal");
    }

    @Test
    void domain_should_not_depend_on_adapter() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..adapter..")
                .check(classes);
    }

    @Test
    void domain_should_not_depend_on_application() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..application..")
                .check(classes);
    }

    @Test
    void domain_should_not_depend_on_infrastructure() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .check(classes);
    }

    @Test
    void application_should_not_depend_on_adapter() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..adapter..")
                .check(classes);
    }

    @Test
    void domain_should_not_use_jakarta_persistence() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..")
                .check(classes);
    }

    @Test
    void application_should_not_use_jakarta_persistence() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..")
                .check(classes);
    }

    @Test
    void domain_should_not_use_spring() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                .check(classes);
    }

    @Test
    void application_services_should_not_use_spring_annotations() {
        noClasses()
                .that().resideInAPackage("..application.service..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework.stereotype..")
                .check(classes);
    }
}
