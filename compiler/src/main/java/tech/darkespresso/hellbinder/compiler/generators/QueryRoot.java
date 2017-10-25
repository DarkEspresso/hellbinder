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
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import javax.annotation.Nonnull;
import javax.lang.model.element.Modifier;
import tech.darkespresso.hellbinder.CloseableList;
import tech.darkespresso.hellbinder.compiler.BoundField;
import tech.darkespresso.hellbinder.compiler.utils.CollectionUtils;

/**
 * Contains the {@link TypeSpec} representing the {@code QueryRoot} interface for the given entity.
 */
public class QueryRoot {
  static final ClassName NAME = ClassName.get("", "QueryRoot");

  private QueryRoot() {
    throw new UnsupportedOperationException();
  }

  public static TypeSpec generate(@Nonnull TypeName entityType, @Nonnull List<BoundField> fields) {
    entityType = Preconditions.checkNotNull(entityType);
    fields = Preconditions.checkNotNull(fields);
    TypeSpec.Builder builder = TypeSpec.interfaceBuilder(NAME).addModifiers(Modifier.PUBLIC);

    MethodSpec get =
        MethodSpec.methodBuilder("get")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(CONTENT_RESOLVER, "contentResolver")
            .returns(ParameterizedTypeName.get(ClassName.get(CloseableList.class), entityType))
            .build();
    MethodSpec count =
        MethodSpec.methodBuilder("count")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(CONTENT_RESOLVER, "contentResolver")
            .returns(TypeName.INT)
            .build();
    builder.addMethods(ImmutableList.of(get, count));

    boolean generateWhere = fields.stream().anyMatch(BoundField::canBeConstrained);
    if (generateWhere) {
      MethodSpec where =
          MethodSpec.methodBuilder("where")
              .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .returns(QueryBuilder.NAME)
              .build();
      builder.addMethod(where);
    }

    if (fields.stream().anyMatch(BoundField::canBeUsedForSorting)) {
      MethodSpec sortBy =
          MethodSpec.methodBuilder("sortBy")
              .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .returns(OrderBuilder.NAME)
              .build();
      builder.addMethod(sortBy);
    }
    BoundField id =
        fields.stream().filter(BoundField::isId).collect(CollectionUtils.uniqueOrNull());
    if (id != null) {
      Preconditions.checkState(generateWhere);
      MethodSpec getById =
          MethodSpec.methodBuilder("getById")
              .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .addParameter(CONTENT_RESOLVER, "contentResolver")
              .addParameter(id.getType(), "id")
              .returns(entityType)
              .build();
      builder.addMethod(getById);
    }

    return builder.build();
  }
}
