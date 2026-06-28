package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.PluginActivationService;

import java.util.HashSet;
import java.util.List;

public class MetaClassCompletionLightTest extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ServiceManager.getService(getProject(), PluginActivationService.class).setPluginActive(true);
        getProject().getService(MetaClassBindingTypeCache.class).clear();
    }

    public void testMemberCompletionWithoutDuplicates() {
        myFixture.addClass("""
            package ru.naumen.core.shared.dto;
            public interface ISDtObject {}
            """);
        myFixture.addClass("""
            package ru.naumen.core.server.script.spi;
            public abstract class Employee_SDO implements ru.naumen.core.shared.dto.ISDtObject {
                public String lastName;
                public String firstName;
                public String getLastName() { return lastName; }
                public String getFirstName() { return firstName; }
            }
            """);

        myFixture.configureByText("script.groovy", """
            import ru.naumen.core.shared.dto.ISDtObject

            // nsmp:meta employee
            ISDtObject subject = utils.get('employee$123')

            subject.l<caret>
            """);

        myFixture.completeBasic();
        List<String> lookups = myFixture.getLookupElementStrings();
        assertNotNull("Completion variants are null", lookups);
        assertTrue(
            "Expected lastName in completion, got: " + lookups,
            lookups.contains("lastName")
        );
        assertEquals(
            "Completion contains duplicates: " + lookups,
            lookups.size(),
            new HashSet<>(lookups).size()
        );
    }

    public void testNestedObjectFieldCompletionWithoutDuplicates() {
        myFixture.addClass("""
            package ru.naumen.core.shared.dto;
            public interface ISDtObject {}
            """);
        myFixture.addClass("""
            package ru.naumen.core.server.script.spi;
            public abstract class Manager_SDO implements ru.naumen.core.shared.dto.ISDtObject {
                public String firstName;
                public String getFirstName() { return firstName; }
            }
            """);
        myFixture.addClass("""
            package ru.naumen.core.server.script.spi;
            public abstract class Employee_SDO implements ru.naumen.core.shared.dto.ISDtObject {
                public Manager_SDO manager;
                public Manager_SDO getManager() { return manager; }
            }
            """);

        myFixture.configureByText("script.groovy", """
            import ru.naumen.core.shared.dto.ISDtObject

            // nsmp:meta employee
            ISDtObject subject = utils.get('employee$123')

            subject.manager.f<caret>
            """);

        myFixture.completeBasic();
        List<String> lookups = myFixture.getLookupElementStrings();
        assertNotNull("Completion variants are null", lookups);
        assertTrue(
            "Expected firstName in nested completion, got: " + lookups,
            lookups.contains("firstName")
        );
        assertEquals(
            "Nested completion contains duplicates: " + lookups,
            lookups.size(),
            new HashSet<>(lookups).size()
        );
    }
}
