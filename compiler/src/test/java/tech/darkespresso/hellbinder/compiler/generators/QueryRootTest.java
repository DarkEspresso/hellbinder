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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import org.junit.Test;
import tech.darkespresso.hellbinder.compiler.BoundField;
/** Tests for {@link QueryRoot} */
public class QueryRootTest {
  @Test
  public void generate() {
    TypeName entityType = ClassName.get("", "Foo");
    BoundField id = mock(BoundField.class);
    when(id.canBeConstrained()).thenReturn(true);
    when(id.isId()).thenReturn(true);
    when(id.getType()).thenReturn(TypeName.LONG);
    BoundField sortable = mock(BoundField.class);
    when(sortable.canBeUsedForSorting()).thenReturn(true);

    TypeSpec actual = QueryRoot.generate(entityType, ImmutableList.of(id, sortable));

    assertEquals(5, actual.methodSpecs.size());

    MethodSpec count =
        MethodSpec.methodBuilder("count")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ClassName.get("android.content", "ContentResolver"), "contentResolver")
            .returns(TypeName.INT)
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(count::equals));

    MethodSpec get =
        MethodSpec.methodBuilder("get")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ClassName.get("android.content", "ContentResolver"), "contentResolver")
            .returns(
                ParameterizedTypeName.get(
                    ClassName.get("tech.darkespresso.hellbinder", "CloseableList"),
                    ClassName.get("", "Foo")))
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(get::equals));

    MethodSpec sortBy =
        MethodSpec.methodBuilder("sortBy")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(ClassName.get("", "OrderBuilder"))
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(sortBy::equals));

    MethodSpec where =
        MethodSpec.methodBuilder("where")
            .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
            .returns(ClassName.get("", "QueryBuilder"))
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(where::equals));

    MethodSpec getById =
        MethodSpec.methodBuilder("getById")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ClassName.get("android.content", "ContentResolver"), "contentResolver")
            .addParameter(TypeName.LONG, "id")
            .returns(ClassName.get("", "Foo"))
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(getById::equals));
  }

  @Test
  public void generate_noWhereNoSortByNoId() {
    TypeName entityType = ClassName.get("", "Foo");

    TypeSpec actual = QueryRoot.generate(entityType, ImmutableList.of(mock(BoundField.class)));

    assertEquals(2, actual.methodSpecs.size());

    MethodSpec count =
        MethodSpec.methodBuilder("count")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ClassName.get("android.content", "ContentResolver"), "contentResolver")
            .returns(TypeName.INT)
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(count::equals));

    MethodSpec get =
        MethodSpec.methodBuilder("get")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ClassName.get("android.content", "ContentResolver"), "contentResolver")
            .returns(
                ParameterizedTypeName.get(
                    ClassName.get("tech.darkespresso.hellbinder", "CloseableList"),
                    ClassName.get("", "Foo")))
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(get::equals));
  }

  @Test
  public void generate_whereNoSortByNoId() {
    TypeName entityType = ClassName.get("", "Foo");
    BoundField constraint = mock(BoundField.class);
    when(constraint.canBeConstrained()).thenReturn(true);

    TypeSpec actual = QueryRoot.generate(entityType, ImmutableList.of(constraint));

    assertEquals(3, actual.methodSpecs.size());

    MethodSpec count =
        MethodSpec.methodBuilder("count")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ClassName.get("android.content", "ContentResolver"), "contentResolver")
            .returns(TypeName.INT)
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(count::equals));

    MethodSpec get =
        MethodSpec.methodBuilder("get")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ClassName.get("android.content", "ContentResolver"), "contentResolver")
            .returns(
                ParameterizedTypeName.get(
                    ClassName.get("tech.darkespresso.hellbinder", "CloseableList"),
                    ClassName.get("", "Foo")))
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(get::equals));

    MethodSpec where =
        MethodSpec.methodBuilder("where")
            .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
            .returns(ClassName.get("", "QueryBuilder"))
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(where::equals));
  }

  @Test
  public void generate_noWhereSortByNoId() {
    TypeName entityType = ClassName.get("", "Foo");
    BoundField sortable = mock(BoundField.class);
    when(sortable.canBeUsedForSorting()).thenReturn(true);

    TypeSpec actual = QueryRoot.generate(entityType, ImmutableList.of(sortable));

    assertEquals(3, actual.methodSpecs.size());

    MethodSpec count =
        MethodSpec.methodBuilder("count")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ClassName.get("android.content", "ContentResolver"), "contentResolver")
            .returns(TypeName.INT)
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(count::equals));

    MethodSpec get =
        MethodSpec.methodBuilder("get")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ClassName.get("android.content", "ContentResolver"), "contentResolver")
            .returns(
                ParameterizedTypeName.get(
                    ClassName.get("tech.darkespresso.hellbinder", "CloseableList"),
                    ClassName.get("", "Foo")))
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(get::equals));

    MethodSpec sortBy =
        MethodSpec.methodBuilder("sortBy")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(ClassName.get("", "OrderBuilder"))
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(sortBy::equals));
  }

  @Test
  public void generate_noEntityType() {
    BoundField constraint = mock(BoundField.class);
    when(constraint.canBeConstrained()).thenReturn(true);
    BoundField sortable = mock(BoundField.class);
    when(sortable.canBeUsedForSorting()).thenReturn(true);
    try {
      QueryRoot.generate(null, ImmutableList.of(constraint, sortable));
      fail();
    } catch (NullPointerException e) {
      // expected.
    }
  }
}
