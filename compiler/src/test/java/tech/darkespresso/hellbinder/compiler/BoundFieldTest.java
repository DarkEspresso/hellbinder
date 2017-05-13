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

package tech.darkespresso.hellbinder.compiler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;
import javax.tools.Diagnostic;
import org.junit.Before;
import org.junit.Test;
import tech.darkespresso.hellbinder.Operator;
import tech.darkespresso.hellbinder.Order;
import tech.darkespresso.hellbinder.annotations.Column;
import tech.darkespresso.hellbinder.annotations.Constraint;
import tech.darkespresso.hellbinder.annotations.SortCriterion;
import tech.darkespresso.hellbinder.compiler.generators.Constraining;
import tech.darkespresso.hellbinder.compiler.generators.Ordering;

/** Tests for {@link BoundField}. */
public class BoundFieldTest {
  private VariableElement mockVariableElement;

  @Before
  public void setUp() {
    // Set up a VariableElement typeName like:
    // ...
    //   @Constraint
    //   @Column("_bar")
    //   public int bar;
    Column mockColumn = mock(Column.class);
    when(mockColumn.value()).thenReturn("_bar");
    PrimitiveType fieldType = mock(PrimitiveType.class);
    when(fieldType.getKind()).thenReturn(TypeKind.INT);
    when(fieldType.accept(any(), any()))
        .thenAnswer(
            invocation -> {
              TypeVisitor<?, ?> typeVisitor = invocation.getArgument(0);
              return typeVisitor.visitPrimitive(fieldType, invocation.getArgument(1));
            });

    Name mockName = mock(Name.class);
    when(mockName.toString()).thenReturn("bar");

    mockVariableElement = mock(VariableElement.class);
    when(mockVariableElement.asType()).thenReturn(fieldType);
    when(mockVariableElement.getModifiers()).thenReturn(ImmutableSet.of(Modifier.PUBLIC));
    when(mockVariableElement.getAnnotation(Column.class)).thenReturn(mockColumn);
    when(mockVariableElement.getSimpleName()).thenReturn(mockName);
  }

  @Test
  public void constructor_sortableConstraint() {
    when(mockVariableElement.getAnnotation(Constraint.class)).thenReturn(mock(Constraint.class));
    when(mockVariableElement.getAnnotation(SortCriterion.class))
        .thenReturn(mock(SortCriterion.class));
    BoundField boundField = new BoundField(mockVariableElement);
    assertEquals("_bar", boundField.getColumn());
    assertEquals(TypeName.INT, boundField.getType());

    MethodSpec expectedConstraint =
        MethodSpec.methodBuilder("bar")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(Operator.class, "op")
            .addParameter(TypeName.INT, "value")
            .returns(Constraining.NAME)
            .build();
    assertEquals(expectedConstraint, boundField.getConstraint());

    MethodSpec expectedSortBy =
        MethodSpec.methodBuilder("bar")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(Order.class, "order")
            .returns(Ordering.NAME)
            .build();
    assertEquals(expectedSortBy, boundField.getSortBy());
  }

  @Test
  public void constructor_sortableNoConstraint() {
    when(mockVariableElement.getAnnotation(SortCriterion.class))
        .thenReturn(mock(SortCriterion.class));
    BoundField boundField = new BoundField(mockVariableElement);
    assertEquals("_bar", boundField.getColumn());
    assertEquals(TypeName.INT, boundField.getType());

    assertNull(boundField.getConstraint());

    MethodSpec expectedSortBy =
        MethodSpec.methodBuilder("bar")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(Order.class, "order")
            .returns(Ordering.NAME)
            .build();
    assertEquals(expectedSortBy, boundField.getSortBy());
  }

  @Test
  public void getFieldName() {
    BoundField boundField = new BoundField(mockVariableElement);
    assertEquals("bar", boundField.getFieldName());
  }

  @Test
  public void isValid_validElement() {
    assertTrue(BoundField.isValid(mockVariableElement, mock(Messager.class)));
  }

  @Test
  public void isValid_noColumnAnnotation() {
    when(mockVariableElement.getAnnotation(Column.class)).thenReturn(null);
    assertFalse(BoundField.isValid(mockVariableElement, mock(Messager.class)));
  }

  @Test
  public void isValid_nonPublicField() {
    when(mockVariableElement.getModifiers()).thenReturn(ImmutableSet.of());
    Messager messager = mock(Messager.class);
    assertFalse(BoundField.isValid(mockVariableElement, messager));
    verify(messager)
        .printMessage(
            Diagnostic.Kind.ERROR,
            "Fields annotated with @interface tech.darkespresso.hellbinder.annotations.Column "
                + "must be public.",
            mockVariableElement);
  }

  @Test
  public void isValid_staticField() {
    when(mockVariableElement.getModifiers())
        .thenReturn(ImmutableSet.of(Modifier.PUBLIC, Modifier.STATIC));
    Messager messager = mock(Messager.class);
    assertFalse(BoundField.isValid(mockVariableElement, messager));
    verify(messager)
        .printMessage(
            Diagnostic.Kind.ERROR,
            "Fields annotated with @interface tech.darkespresso.hellbinder.annotations.Column "
                + "cannot be static.",
            mockVariableElement);
  }

  @Test
  public void isValid_finalField() {
    when(mockVariableElement.getModifiers())
        .thenReturn(ImmutableSet.of(Modifier.PUBLIC, Modifier.FINAL));
    Messager messager = mock(Messager.class);
    assertFalse(BoundField.isValid(mockVariableElement, messager));
    verify(messager)
        .printMessage(
            Diagnostic.Kind.ERROR,
            "Fields annotated with @interface tech.darkespresso.hellbinder.annotations.Column "
                + "cannot be final.",
            mockVariableElement);
  }

  @Test
  public void isValid_reportsAllErrors() {
    when(mockVariableElement.getModifiers())
        .thenReturn(ImmutableSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL));
    Messager messager = mock(Messager.class);
    assertFalse(BoundField.isValid(mockVariableElement, messager));
    verify(messager)
        .printMessage(
            Diagnostic.Kind.ERROR,
            "Fields annotated with @interface tech.darkespresso.hellbinder.annotations.Column "
                + "cannot be static.",
            mockVariableElement);
    verify(messager)
        .printMessage(
            Diagnostic.Kind.ERROR,
            "Fields annotated with @interface tech.darkespresso.hellbinder.annotations.Column "
                + "cannot be final.",
            mockVariableElement);
    verify(messager)
        .printMessage(
            Diagnostic.Kind.ERROR,
            "Fields annotated with @interface tech.darkespresso.hellbinder.annotations.Column "
                + "must be public.",
            mockVariableElement);
  }
}
