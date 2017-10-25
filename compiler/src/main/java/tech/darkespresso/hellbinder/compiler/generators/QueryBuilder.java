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

import com.google.common.base.Preconditions;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.lang.model.element.Modifier;
import tech.darkespresso.hellbinder.Operator;
import tech.darkespresso.hellbinder.annotations.Column;
import tech.darkespresso.hellbinder.annotations.Constraint;
import tech.darkespresso.hellbinder.annotations.ContentProviderEntity;
import tech.darkespresso.hellbinder.compiler.BoundField;

/**
 * Contains the name and the generate method for the interface returned when it is possible to
 * specify constraints.
 *
 * <p>Suppose the class annotated with {@link ContentProviderEntity ContentProviderEntity} is named
 * {@code Foo}, and it has a {@link Column Column }-annotated {@code int} field named {@code bar}
 * which is also annotated by {@link Constraint Constraint}.
 *
 * <p>The generated interface will be:
 *
 * <pre>{@code
 * public interface QueryBuilder {
 *     Constraining bar(Operator op, int value);
 * }
 * }</pre>
 *
 * where {@link Operator op} is used to specify what kind of relation must there be between the
 * field and the given value.
 */
public class QueryBuilder {
  static final ClassName NAME = ClassName.get("", "QueryBuilder");

  private QueryBuilder() {
    throw new UnsupportedOperationException();
  }

  public static TypeSpec generate(@Nonnull List<BoundField> fields) {
    Preconditions.checkArgument(!fields.isEmpty());
    return TypeSpec.interfaceBuilder(NAME)
        .addModifiers(Modifier.PUBLIC)
        .addMethods(
            fields
                .stream()
                .filter(BoundField::canBeConstrained)
                .map(BoundField::getConstraint)
                .collect(Collectors.toList()))
        .addMethods(
            fields
                .stream()
                .filter(BoundField::isNullable)
                .map(BoundField::getIsNull)
                .collect(Collectors.toList()))
        .build();
  }
}
