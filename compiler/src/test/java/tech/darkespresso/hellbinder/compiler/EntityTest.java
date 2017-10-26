package tech.darkespresso.hellbinder.compiler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import java.util.List;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import org.junit.Test;
import tech.darkespresso.hellbinder.annotations.Column;
import tech.darkespresso.hellbinder.annotations.ContentProviderEntity;
import tech.darkespresso.hellbinder.annotations.Id;

/** Tests for {@link Entity}. */
public class EntityTest {
  private Messager mockMessager = mock(Messager.class);

  @Test
  public void constructor() {
    String source =
        "package tech.darkespresso;\n\n"
            + "import tech.darkespresso.hellbinder.annotations.*;\n"
            + "@ContentProviderEntity(\"Fooz\")\n"
            + "class Foo {\n"
            + "  @Column(\"_bar\") public int bar;\n"
            + "  public String willBeIgnored;\n"
            + "}\n";
    Entity entity;

    entity =
        TestUtils.fromSource(
            source,
            ContentProviderEntity.class,
            (element, processingEnv) -> {
              try {
                return new Entity(
                    (TypeElement) element, processingEnv.getTypeUtils(), mockMessager);
              } catch (ProcessingException e) {
                fail();
                return null;
              }
            });

    assertEquals("tech.darkespresso.Foo", entity.getElement().getQualifiedName().toString());
    assertEquals(ClassName.get("tech.darkespresso", "Fooz"), entity.getCollectionClassName());
    assertEquals(ClassName.get("tech.darkespresso", "Foo"), entity.getTypeName());

    List<Element> columns = entity.getElementsAnnotatedWith(Column.class);
    assertEquals(1, columns.size());
    assertEquals("bar", columns.get(0).getSimpleName().toString());
    assertEquals(1, entity.getFields().size());

    FieldSpec expectedProjection =
        FieldSpec.builder(String[].class, "PROJECTION")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("{ \"_bar\" }")
            .build();
    assertEquals(expectedProjection, entity.getProjection());

    MethodSpec expectedBind =
        MethodSpec.methodBuilder("bind")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .addParameter(ClassName.get("tech.darkespresso", "Foo"), "entity")
            .addParameter(AndroidClasses.CURSOR, "cursor")
            .addStatement("entity.bar = cursor.getInt(0)")
            .build();
    assertEquals(expectedBind, entity.getBindMethod());
  }

  @Test
  public void constructor_errorIfMultipleIds() {
    String source =
        "package tech.darkespresso;\n\n"
            + "import tech.darkespresso.hellbinder.annotations.*;\n"
            + "@ContentProviderEntity(\"Fooz\")\n"
            + "class Foo {\n"
            + "  @Id @Column(\"_bar\") public int bar;\n"
            + "  @Id @Column(\"_id\") public long id;\n"
            + "}\n";

    TestUtils.fromSource(
        source,
        ContentProviderEntity.class,
        (element, processingEnv) -> {
          try {
            return new Entity((TypeElement) element, processingEnv.getTypeUtils(), mockMessager);
          } catch (ProcessingException e) {
            fail();
            return null;
          }
        });
    verify(mockMessager)
        .printMessage(eq(Kind.ERROR), eq("more than 1 field annotated with " + Id.class), any());
  }

  @Test
  public void constructor_errorIfDuplicateColumnName() {
    String source =
        "package tech.darkespresso;\n\n"
            + "import tech.darkespresso.hellbinder.annotations.*;\n"
            + "@ContentProviderEntity(\"Fooz\")\n"
            + "class Foo {\n"
            + "  @Column(\"_bar\") public int bar;\n"
            + "  @Column(\"_bar\") public long foo;\n"
            + "}\n";

    TestUtils.fromSource(
        source,
        ContentProviderEntity.class,
        (element, processingEnv) -> {
          try {
            return new Entity((TypeElement) element, processingEnv.getTypeUtils(), mockMessager);
          } catch (ProcessingException e) {
            fail();
            return null;
          }
        });
    verify(mockMessager)
        .printMessage(eq(Kind.ERROR), eq("Column _bar already mapped to field bar"), any());
  }

  @Test
  public void constructor_throwsIfNestedClass() {
    String source =
        "package tech.darkespresso;\n\n"
            + "import tech.darkespresso.hellbinder.annotations.*;\n"
            + "class Container {\n"
            + "  @ContentProviderEntity(\"Fooz\")\n"
            + "  class Foo {\n"
            + "    @Column(\"_bar\") public int bar;\n"
            + "  }\n"
            + "}\n";

    TestUtils.fromSource(
        source,
        ContentProviderEntity.class,
        (element, processingEnv) -> {
          try {
            new Entity((TypeElement) element, processingEnv.getTypeUtils(), mockMessager);
            fail();
          } catch (ProcessingException e) {
            // success
          }
          return null;
        });
  }
}
