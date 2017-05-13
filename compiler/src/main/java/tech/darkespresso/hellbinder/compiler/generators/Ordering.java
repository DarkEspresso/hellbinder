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

import static tech.darkespresso.hellbinder.compiler.AndroidClasses.CONTENT_RESOLVER;

import com.google.common.base.Preconditions;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import javax.annotation.Nonnull;
import javax.lang.model.element.Modifier;
import tech.darkespresso.hellbinder.QueryExecutor;
/**
 * Contains the name and the generate method for the interface returned when a sort criterion has
 * been established.
 *
 * <p>Suppose the class annotated with {@link
 * tech.darkespresso.hellbinder.annotations.ContentProviderEntity} is named {@code Foo}.
 *
 * <p>The generated interface will be:
 *
 * <pre>{@code
 * public interface Ordering extends QueryExecutor<Foo, ContentResolver> {
 *     OrderBuilder thenBy();
 * }
 * }</pre>
 *
 * where {@code OrderBuilder} is the interface exposing the methods to establish sorting criteria.
 */
public class Ordering {
  public static final ClassName NAME = ClassName.get("", "Ordering");

  private Ordering() {
    throw new UnsupportedOperationException();
  }

  public static TypeSpec generate(@Nonnull TypeName entityType) {
    entityType = Preconditions.checkNotNull(entityType);
    MethodSpec thenBy =
        MethodSpec.methodBuilder("thenBy")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(OrderBuilder.NAME)
            .build();
    return TypeSpec.interfaceBuilder(NAME.simpleName())
        .addModifiers(Modifier.PUBLIC)
        .addSuperinterface(
            ParameterizedTypeName.get(
                ClassName.get(QueryExecutor.class), entityType, CONTENT_RESOLVER))
        .addMethod(thenBy)
        .build();
  }
}
