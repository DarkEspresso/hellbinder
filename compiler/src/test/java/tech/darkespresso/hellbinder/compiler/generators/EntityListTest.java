/*
 * Copyright (c) 2017, DarkEspresso
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package tech.darkespresso.hellbinder.compiler.generators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import org.junit.Test;
/** Tests for {@link EntityList} */
public class EntityListTest {
  @Test
  public void generate() {
    TypeName entityType = ClassName.get("", "Foo");
    MethodSpec bind =
        MethodSpec.methodBuilder("bind")
            .addParameter(entityType, "entity")
            .addParameter(ClassName.get("android.database", "Cursor"), "cursor")
            .build();

    TypeSpec actual = EntityList.generate(entityType, bind);

    assertEquals(1, actual.fieldSpecs.size());
    assertTrue(actual.fieldSpecs.stream().anyMatch(f -> "mCursor".equals(f.name)));

    assertEquals(5, actual.methodSpecs.size());

    MethodSpec expectedSize =
        MethodSpec.methodBuilder("size")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(TypeName.INT)
            .addStatement("return mCursor.getCount()")
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(expectedSize::equals));

    MethodSpec expectedGetIndexOnly =
        MethodSpec.methodBuilder("get")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.INT, "index")
            .returns(ClassName.get("", "Foo"))
            .addStatement("return get(index, null)")
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(expectedGetIndexOnly::equals));

    MethodSpec expectedClose =
        MethodSpec.methodBuilder("close")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addStatement("mCursor.close()")
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(expectedClose::equals));

    MethodSpec expectedGetWithEntity =
        MethodSpec.methodBuilder("get")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.INT, "index")
            .addParameter(entityType, "entity")
            .returns(ClassName.get("", "Foo"))
            .beginControlFlow("if (index < 0 || index >= size())")
            .addStatement("throw new java.lang.IndexOutOfBoundsException()")
            .endControlFlow()
            .addStatement("entity = entity == null ? new Foo() : entity")
            .addStatement("mCursor.moveToPosition(index)")
            .addStatement("bind(entity, mCursor)")
            .addStatement("return entity")
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(expectedGetWithEntity::equals));
  }

  @Test
  public void generate_nullEntityType() {
    MethodSpec bind =
        MethodSpec.methodBuilder("bind")
            .addParameter(ClassName.get("", "Foo"), "entity")
            .addParameter(ClassName.get("android.database", "Cursor"), "cursor")
            .build();
    try {
      EntityList.generate(null, bind);
      fail();
    } catch (NullPointerException e) {
      // success.
    }
  }

  @Test
  public void generate_nullBind() {
    TypeName entityType = ClassName.get("", "Foo");
    try {
      EntityList.generate(entityType, null);
      fail();
    } catch (NullPointerException e) {
      // success.
    }
  }

  @Test
  public void generate_bindHasWrongNumberOfParameters() {
    TypeName entityType = ClassName.get("", "Foo");
    MethodSpec bind =
        MethodSpec.methodBuilder("bind")
            .addParameter(ClassName.get("", "Foo"), "entity")
            .addParameter(ClassName.get("android.database", "Cursor"), "cursor")
            .addParameter(TypeName.INT, "whatAmIFor")
            .build();
    try {
      EntityList.generate(entityType, bind);
      fail();
    } catch (IllegalArgumentException e) {
      // success.
    }
  }

  @Test
  public void generate_bindEntityTypeMismatch() {
    TypeName entityType = ClassName.get("", "Foo");
    MethodSpec bind =
        MethodSpec.methodBuilder("bind")
            .addParameter(ClassName.get("", "Bar"), "entity")
            .addParameter(ClassName.get("android.database", "Cursor"), "cursor")
            .build();
    try {
      EntityList.generate(entityType, bind);
      fail();
    } catch (IllegalArgumentException e) {
      // success.
    }
  }

  @Test
  public void generate_bindSecondParameterIsNotACursor() {
    TypeName entityType = ClassName.get("", "Foo");
    MethodSpec bind =
        MethodSpec.methodBuilder("bind")
            .addParameter(ClassName.get("", "Bar"), "entity")
            .addParameter(ClassName.get("foo.bar", "FooBar"), "cursor")
            .build();
    try {
      EntityList.generate(entityType, bind);
      fail();
    } catch (IllegalArgumentException e) {
      // success.
    }
  }
}
