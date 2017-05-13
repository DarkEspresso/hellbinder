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

import com.google.common.base.Preconditions;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import tech.darkespresso.hellbinder.Operator;
import tech.darkespresso.hellbinder.Order;
import tech.darkespresso.hellbinder.annotations.Column;
import tech.darkespresso.hellbinder.annotations.Constraint;
import tech.darkespresso.hellbinder.annotations.Id;
import tech.darkespresso.hellbinder.annotations.SortCriterion;
import tech.darkespresso.hellbinder.compiler.generators.Constraining;
import tech.darkespresso.hellbinder.compiler.generators.Ordering;
/** Represents a public, non-final field annotated with {@link Column}. */
public class BoundField {
  private final String column;
  private final TypeName type;
  @Nullable private final MethodSpec constraint;
  @Nullable private final MethodSpec sortBy;
  @Nonnull private final VariableElement field;

  BoundField(@Nonnull VariableElement field) {
    this.field = Preconditions.checkNotNull(field);
    column = Preconditions.checkNotNull(field.getAnnotation(Column.class)).value();
    type = TypeName.get(field.asType());
    if (field.getAnnotation(Constraint.class) != null || field.getAnnotation(Id.class) != null) {
      constraint =
          MethodSpec.methodBuilder(field.getSimpleName().toString())
              .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .addParameter(Operator.class, "op")
              .addParameter(type, "value")
              .returns(Constraining.NAME)
              .build();
    } else {
      constraint = null;
    }
    if (field.getAnnotation(SortCriterion.class) != null) {
      sortBy =
          MethodSpec.methodBuilder(field.getSimpleName().toString())
              .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .addParameter(Order.class, "order")
              .returns(Ordering.NAME)
              .build();
    } else {
      sortBy = null;
    }
  }

  static boolean isValid(VariableElement element, Messager messager) {
    if (element.getAnnotation(Column.class) == null) {
      return false;
    }
    boolean valid = true;
    if (!element.getModifiers().contains(Modifier.PUBLIC)) {
      messager.printMessage(
          Diagnostic.Kind.ERROR,
          String.format("Fields annotated with @%s must be public.", Column.class),
          element);
      valid = false;
    }
    if (element.getModifiers().contains(Modifier.STATIC)) {
      messager.printMessage(
          Diagnostic.Kind.ERROR,
          String.format("Fields annotated with @%s cannot be static.", Column.class),
          element);
      valid = false;
    }
    if (element.getModifiers().contains(Modifier.FINAL)) {
      messager.printMessage(
          Diagnostic.Kind.ERROR,
          String.format("Fields annotated with @%s cannot be final.", Column.class),
          element);
      valid = false;
    }
    return valid;
  }

  public String getColumn() {
    return column;
  }

  public TypeName getType() {
    return type;
  }

  @Nullable
  public MethodSpec getConstraint() {
    return constraint;
  }

  @Nullable
  public MethodSpec getSortBy() {
    return sortBy;
  }

  public String getFieldName() {
    return field.getSimpleName().toString();
  }

  @Nonnull
  public VariableElement getField() {
    return field;
  }

  public boolean canBeConstrained() {
    return constraint != null;
  }

  public boolean canBeUsedForSorting() {
    return sortBy != null;
  }

  public boolean isId() {
    return field.getAnnotation(Id.class) != null;
  }
}
