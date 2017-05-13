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

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import org.junit.Test;
/** Tests for {@link Constraining} */
public class ConstrainingTest {
  @Test
  public void generate_notSortable() {
    TypeName entityType = ClassName.get("", "Foo");

    TypeSpec actual = Constraining.generate(entityType, false);

    assertEquals(2, actual.methodSpecs.size());
    MethodSpec and =
        MethodSpec.methodBuilder("and")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(ClassName.get("", "QueryBuilder"))
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(and::equals));

    MethodSpec or =
        MethodSpec.methodBuilder("or")
            .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
            .returns(ClassName.get("", "QueryBuilder"))
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(or::equals));

    assertEquals(1, actual.superinterfaces.size());
    TypeName expectedSuperinterface =
        ParameterizedTypeName.get(
            ClassName.get("tech.darkespresso.hellbinder", "QueryExecutor"),
            ClassName.get("", "Foo"),
            ClassName.get("android.content", "ContentResolver"));
    assertTrue(actual.superinterfaces.stream().anyMatch(expectedSuperinterface::equals));
  }

  @Test
  public void generate_sortable() {
    TypeName entityType = ClassName.get("", "Foo");

    TypeSpec actual = Constraining.generate(entityType, true);

    assertEquals(3, actual.methodSpecs.size());

    MethodSpec and =
        MethodSpec.methodBuilder("and")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(ClassName.get("", "QueryBuilder"))
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(and::equals));

    MethodSpec or =
        MethodSpec.methodBuilder("or")
            .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
            .returns(ClassName.get("", "QueryBuilder"))
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(or::equals));

    MethodSpec sortBy =
        MethodSpec.methodBuilder("sortBy")
            .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
            .returns(ClassName.get("", "OrderBuilder"))
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(sortBy::equals));

    assertEquals(1, actual.superinterfaces.size());
    TypeName expectedSuperinterface =
        ParameterizedTypeName.get(
            ClassName.get("tech.darkespresso.hellbinder", "QueryExecutor"),
            ClassName.get("", "Foo"),
            ClassName.get("android.content", "ContentResolver"));
    assertTrue(actual.superinterfaces.stream().anyMatch(expectedSuperinterface::equals));
  }

  @Test
  public void generate_nullEntityType() {
    try {
      Constraining.generate(null, false);
      fail();
    } catch (NullPointerException e) {
      // expected.
    }
  }
}
