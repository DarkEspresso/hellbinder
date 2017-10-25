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
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.AbstractList;
import javax.annotation.Nonnull;
import javax.lang.model.element.Modifier;
import tech.darkespresso.hellbinder.CloseableList;
import tech.darkespresso.hellbinder.compiler.AndroidClasses;

/**
 * Contains the method to generate a subclass of {@link AbstractList} that wraps a cursor.
 *
 * <p>The constructor takes two argument: a cursor, and an optional instance of the entity class. If
 * the latter is present, this instance will always be populated with data extracted from the
 * cursor, and will <b>always</b> be returned by {@link AbstractList#get(int) get(int i)}.
 * Otherwise, a new instance of the entity class is returned every time.
 */
public class EntityList {
  public static final ClassName NAME = ClassName.get("", "EntityList");

  private EntityList() {
    throw new UnsupportedOperationException();
  }

  public static TypeSpec generate(@Nonnull TypeName entityType, @Nonnull MethodSpec bind) {
    entityType = Preconditions.checkNotNull(entityType);
    bind = Preconditions.checkNotNull(bind);
    Preconditions.checkArgument(bind.parameters.size() == 2);
    Preconditions.checkArgument(bind.parameters.get(0).type.equals(entityType));
    Preconditions.checkArgument(bind.parameters.get(1).type.equals(AndroidClasses.CURSOR));

    FieldSpec cursor =
        FieldSpec.builder(AndroidClasses.CURSOR, "mCursor", Modifier.PRIVATE, Modifier.FINAL)
            .build();

    MethodSpec constructor =
        MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .addParameter(AndroidClasses.CURSOR, "cursor")
            .addStatement("$N = cursor", cursor)
            .addStatement("$N.moveToFirst()", cursor)
            .build();
    MethodSpec size =
        MethodSpec.methodBuilder("size")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(TypeName.INT)
            .addStatement("return $N.getCount()", cursor)
            .build();
    MethodSpec get =
        MethodSpec.methodBuilder("get")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.INT, "index")
            .returns(entityType)
            .addStatement("return get(index, null)")
            .build();
    MethodSpec getWithEntity =
        MethodSpec.methodBuilder("get")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.INT, "index")
            .addParameter(entityType, "entity")
            .returns(entityType)
            .beginControlFlow("if (index < 0 || index >= size())")
            .addStatement("throw new $T()", IndexOutOfBoundsException.class)
            .endControlFlow()
            .addStatement("entity = entity == null ? new $T() : entity", entityType)
            .addStatement("$N.moveToPosition(index)", cursor)
            .addStatement("$N(entity, $N)", bind, cursor)
            .addStatement("return entity")
            .build();
    MethodSpec close =
        MethodSpec.methodBuilder("close")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addStatement("$N.close()", cursor)
            .build();

    return TypeSpec.classBuilder(NAME)
        .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
        .superclass(ParameterizedTypeName.get(ClassName.get(AbstractList.class), entityType))
        .addSuperinterface(
            ParameterizedTypeName.get(ClassName.get(CloseableList.class), entityType))
        .addMethods(ImmutableList.of(constructor, size, get, getWithEntity, close))
        .addFields(ImmutableList.of(cursor))
        .build();
  }
}
