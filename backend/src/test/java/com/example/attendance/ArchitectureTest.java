package com.example.attendance;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
    packages = "com.example.attendance",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

    @ArchTest
    static final ArchRule controllers_should_not_access_repositories =
        noClasses().that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().resideInAPackage("..repository..");

    @ArchTest
    static final ArchRule entities_should_not_depend_on_other_layers =
        noClasses().that().resideInAPackage("..entity..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..controller..", "..service..", "..repository..", "..config.."
            );

    @ArchTest
    static final ArchRule services_should_not_depend_on_controllers =
        noClasses().that().resideInAPackage("..service..")
            .should().dependOnClassesThat().resideInAPackage("..controller..");
}
