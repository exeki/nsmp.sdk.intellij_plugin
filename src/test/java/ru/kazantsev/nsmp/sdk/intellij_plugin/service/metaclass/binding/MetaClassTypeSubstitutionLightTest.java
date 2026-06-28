package ru.kazantsev.nsmp.sdk.intellij_plugin.service.metaclass.binding;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import ru.kazantsev.nsmp.sdk.intellij_plugin.service.ui.PluginActivationService;

public class MetaClassTypeSubstitutionLightTest extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ServiceManager.getService(getProject(), PluginActivationService.class).setPluginActive(true);
        getProject().getService(MetaClassBindingTypeCache.class).clear();
    }

    public void testResolveServiceSubstitutesExplicitlyTypedVariable() {
        myFixture.addClass("""
            package ru.naumen.core.shared.dto;
            public interface ISDtObject {}
            """);
        myFixture.addClass("""
            package ru.naumen.core.server.script.spi;
            public abstract class Employee_SDO implements ru.naumen.core.shared.dto.ISDtObject {
                public String lastName;
            }
            """);

        myFixture.configureByText("script.groovy", """
            import ru.naumen.core.shared.dto.ISDtObject

            // nsmp:meta employee
            ISDtObject subject = utils.get('employee$123')

            subject
            """);

        GroovyFile file = (GroovyFile) myFixture.getFile();
        GrReferenceExpression subjectReference = PsiTreeUtil.findChildrenOfType(file, GrReferenceExpression.class)
            .stream()
            .filter(ref -> "subject".equals(ref.getReferenceName()) && ref.getQualifierExpression() == null)
            .findFirst()
            .orElseThrow();

        MetaClassTypeResolveService resolveService = getProject().getService(MetaClassTypeResolveService.class);
        assertTrue("Plugin should be active in test", resolveService.isActive());

        PsiType resolvedType = resolveService.resolveForReference(subjectReference, null);
        assertNotNull("MetaClassTypeResolveService returned null", resolvedType);
        assertTrue(
            "Expected Employee_SDO, got " + resolvedType.getCanonicalText(),
            resolvedType.getCanonicalText().endsWith("Employee_SDO")
        );

        MetaClassReferenceTypeCalculator calculator = new MetaClassReferenceTypeCalculator();
        PsiType calculatorType = calculator.getType(subjectReference);
        assertNotNull("Type calculator returned null", calculatorType);
        assertTrue(
            "Calculator expected Employee_SDO, got " + calculatorType.getCanonicalText(),
            calculatorType.getCanonicalText().endsWith("Employee_SDO")
        );

        PsiType referenceType = subjectReference.getType();
        assertNotNull("Reference type is null", referenceType);
        assertTrue(
            "Reference expected Employee_SDO, got " + referenceType.getCanonicalText(),
            referenceType.getCanonicalText().endsWith("Employee_SDO")
        );
    }

    public void testMemberAccessUsesSubstitutedType() {
        myFixture.addClass("""
            package ru.naumen.core.shared.dto;
            public interface ISDtObject {}
            """);
        myFixture.addClass("""
            package ru.naumen.core.server.script.spi;
            public abstract class Employee_SDO implements ru.naumen.core.shared.dto.ISDtObject {
                public String lastName;
            }
            """);

        myFixture.configureByText("script.groovy", """
            import ru.naumen.core.shared.dto.ISDtObject

            // nsmp:meta employee
            ISDtObject subject = utils.get('employee$123')

            subject.lastName
            """);

        GroovyFile file = (GroovyFile) myFixture.getFile();
        GrReferenceExpression memberReference = PsiTreeUtil.findChildrenOfType(file, GrReferenceExpression.class)
            .stream()
            .filter(ref -> "lastName".equals(ref.getReferenceName()))
            .findFirst()
            .orElseThrow();

        GrReferenceExpression qualifierReference = (GrReferenceExpression) memberReference.getQualifierExpression();
        assertNotNull(qualifierReference);

        PsiType qualifierType = qualifierReference.getType();
        assertNotNull("Qualifier type is null", qualifierType);
        assertTrue(
            "Expected Employee_SDO qualifier, got " + qualifierType.getCanonicalText(),
            qualifierType.getCanonicalText().endsWith("Employee_SDO")
        );
    }

    public void testNestedObjectReferenceUsesStubFieldType() {
        myFixture.addClass("""
            package ru.naumen.core.shared.dto;
            public interface ISDtObject {}
            """);
        myFixture.addClass("""
            package ru.naumen.core.server.script.spi;
            public abstract class Manager_SDO implements ru.naumen.core.shared.dto.ISDtObject {
                public String firstName;
            }
            """);
        myFixture.addClass("""
            package ru.naumen.core.server.script.spi;
            public abstract class Employee_SDO implements ru.naumen.core.shared.dto.ISDtObject {
                public Manager_SDO manager;
            }
            """);

        myFixture.configureByText("script.groovy", """
            import ru.naumen.core.shared.dto.ISDtObject

            // nsmp:meta employee
            ISDtObject subject = utils.get('employee$123')

            subject.manager
            """);

        GroovyFile file = (GroovyFile) myFixture.getFile();
        GrReferenceExpression managerReference = PsiTreeUtil.findChildrenOfType(file, GrReferenceExpression.class)
            .stream()
            .filter(ref -> "manager".equals(ref.getReferenceName()))
            .findFirst()
            .orElseThrow();

        MetaClassTypeResolveService resolveService = getProject().getService(MetaClassTypeResolveService.class);
        PsiType resolvedType = resolveService.resolveForReference(managerReference, null);
        assertNotNull("Nested object type is null", resolvedType);
        assertTrue(
            "Expected Manager_SDO, got " + resolvedType.getCanonicalText(),
            resolvedType.getCanonicalText().endsWith("Manager_SDO")
        );
    }
}
