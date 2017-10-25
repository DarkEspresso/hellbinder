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
<<<<<<< HEAD

import static tech.darkespresso.hellbinder.compiler.utils.CodeGen.override;
import static tech.darkespresso.hellbinder.compiler.utils.CollectionUtils.getUnique;
import static tech.darkespresso.hellbinder.compiler.utils.CollectionUtils.uniqueOrNull;
=======
>>>>>>> b8eae2b... Add <field>IsNull(boolean) in the constraint generator. More tests for BoundField. Fixes some issues with imports.

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.Modifier;
import tech.darkespresso.hellbinder.CloseableList;
import tech.darkespresso.hellbinder.Operator;
import tech.darkespresso.hellbinder.Order;
import tech.darkespresso.hellbinder.QueryExecutor;
import tech.darkespresso.hellbinder.compiler.AndroidClasses;
import tech.darkespresso.hellbinder.compiler.BoundField;
<<<<<<< HEAD
=======
import tech.darkespresso.hellbinder.compiler.utils.CodeGen;
import tech.darkespresso.hellbinder.compiler.utils.CollectionUtils;

>>>>>>> b8eae2b... Add <field>IsNull(boolean) in the constraint generator. More tests for BoundField. Fixes some issues with imports.
/** Class responsible for the creation of the QueryBuilder interface implementation. */
public class QueryBuilderImpl {
  public static final ClassName NAME = ClassName.get("", "QueryBuilderImpl");

  private static final FieldSpec query =
      FieldSpec.builder(StringBuilder.class, "mQuery", Modifier.PRIVATE, Modifier.FINAL)
          .initializer("new $T()", StringBuilder.class)
          .build();

  private static final FieldSpec args =
      FieldSpec.builder(ParameterizedTypeName.get(ArrayList.class, String.class), "mArgs")
          .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
          .initializer("new $T()", ParameterizedTypeName.get(ArrayList.class, String.class))
          .build();

  private static final FieldSpec sortOrder =
      FieldSpec.builder(StringBuilder.class, "mSortOrder", Modifier.PRIVATE, Modifier.FINAL)
          .initializer("new $T()", StringBuilder.class)
          .build();

  private static final FieldSpec uri =
      FieldSpec.builder(AndroidClasses.URI, "mUri", Modifier.PRIVATE, Modifier.FINAL).build();

  public static TypeSpec generate(
      @Nonnull TypeName entityName,
      @Nonnull FieldSpec projection,
      @Nonnull List<BoundField> fields,
      @Nullable TypeSpec queryRoot) {
    entityName = Preconditions.checkNotNull(entityName);
    projection = Preconditions.checkNotNull(projection);
    TypeSpec.Builder builder =
        TypeSpec.classBuilder(NAME).addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC);

    // The QueryBuilderImpl contains 4 private, final fields:
    // StringBuilder mQuery;
    // ArrayList<String> mArgs;
    // StringBuilder mSortOrder;
    // Uri mUri;
    builder.addFields(ImmutableList.of(query, args, sortOrder, uri));

    // mUri is the only field which is set with a constructor parameter.
    MethodSpec constructor =
        MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .addParameter(AndroidClasses.URI, "uri")
            .addStatement("$N = uri", uri)
            .build();
    builder.addMethod(constructor);

    implementQueryExecutor(builder, entityName, projection);
    boolean explicitlyImplementQueryExecutor =
        !(generateConstraints(builder, fields, queryRoot == null)
            | generateSortCriteria(builder, fields));
    if (explicitlyImplementQueryExecutor) {
      builder.addSuperinterface(
          ParameterizedTypeName.get(
              ClassName.get(QueryExecutor.class), entityName, AndroidClasses.CONTENT_RESOLVER));
    }

    if (queryRoot != null) {
<<<<<<< HEAD
      BoundField id = fields.stream().filter(BoundField::isId).collect(uniqueOrNull());
=======
      BoundField id =
          fields.stream().filter(BoundField::isId).collect(CollectionUtils.uniqueOrNull());
>>>>>>> b8eae2b... Add <field>IsNull(boolean) in the constraint generator. More tests for BoundField. Fixes some issues with imports.
      implementQueryRoot(builder, queryRoot, id);
    }
    return builder.build();
  }

  private static boolean generateConstraints(
      @Nonnull TypeSpec.Builder builder,
      @Nonnull List<BoundField> fields,
      boolean implementSortBy) {
    if (fields.stream().noneMatch(BoundField::canBeConstrained)) {
      return false;
    }
    builder
        .addSuperinterface(Constraining.NAME)
        .addSuperinterface(QueryBuilder.NAME)
        .addMethod(generateConstraintKeyword("and"))
        .addMethod(generateConstraintKeyword("or"))
        .addMethods(
            fields
                .stream()
                .filter(BoundField::canBeConstrained)
                .map(QueryBuilderImpl::generateConstraint)
                .collect(Collectors.toList()))
        .addMethods(
            fields
                .stream()
                .filter(BoundField::isNullable)
                .map(QueryBuilderImpl::generateIsNull)
                .collect(Collectors.toList()));
    if (implementSortBy && fields.stream().anyMatch(f -> f.getSortBy() != null)) {
      builder.addMethod(
          MethodSpec.methodBuilder("sortBy")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
              .returns(OrderBuilder.NAME)
              .addStatement("return this")
              .build());
    }
    return true;
  }

  private static MethodSpec generateConstraintKeyword(@Nonnull String methodName) {
    Preconditions.checkArgument("and".equals(methodName) || "or".equals(methodName));
    return MethodSpec.methodBuilder(methodName)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .returns(QueryBuilder.NAME)
        .addStatement("$N.append(\" $L \")", query, methodName.toUpperCase())
        .addStatement("return this")
        .build();
  }

  private static boolean generateSortCriteria(
      @Nonnull TypeSpec.Builder builder, @Nonnull List<BoundField> fields) {
    if (fields.stream().noneMatch(BoundField::canBeUsedForSorting)) {
      return false;
    }

    builder
        .addSuperinterface(Ordering.NAME)
        .addSuperinterface(OrderBuilder.NAME)
        .addMethods(
            fields
                .stream()
                .filter(f -> f.getSortBy() != null)
                .map(QueryBuilderImpl::generateSortCriterion)
                .collect(Collectors.toList()))
        .addMethod(
            MethodSpec.methodBuilder("thenBy")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(OrderBuilder.NAME)
                .addStatement("$N.append(',')", sortOrder)
                .addStatement("return this")
                .build());
    return true;
  }

  private static void implementQueryExecutor(
      @Nonnull TypeSpec.Builder builder,
      @Nonnull TypeName entityName,
      @Nonnull FieldSpec projection) {
    // QueryExecutor<Entity, ContentResolver>'s overrides:
    // CloseableList<Entity> get(ContentResolver contentResolver);
    // int count(ContentResolver contentResolver);
    ParameterSpec contentResolver =
        ParameterSpec.builder(AndroidClasses.CONTENT_RESOLVER, "contentResolver").build();
    MethodSpec get =
        MethodSpec.methodBuilder("get")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addParameter(contentResolver)
            .returns(ParameterizedTypeName.get(ClassName.get(CloseableList.class), entityName))
            .addCode(query("cursor", contentResolver, projection))
            .addStatement("return new $T(cursor)", EntityList.NAME)
            .build();
    MethodSpec count =
        MethodSpec.methodBuilder("count")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addParameter(contentResolver)
            .returns(TypeName.INT)
            .addCode(query("cursor", contentResolver, null))
            .beginControlFlow("try")
            .addStatement("cursor.moveToFirst()")
            .addStatement("return cursor.getInt(0)")
            .nextControlFlow("finally")
            .addStatement("cursor.close()")
            .endControlFlow()
            .build();
    builder.addMethods(ImmutableList.of(get, count));
  }

  private static void implementQueryRoot(
      @Nonnull TypeSpec.Builder builder, @Nonnull TypeSpec queryRoot, @Nullable BoundField id) {
    builder.addSuperinterface(QueryRoot.NAME);
    Optional<MethodSpec> where =
        queryRoot.methodSpecs.stream().filter(m -> "where".equals(m.name)).findAny();
<<<<<<< HEAD
    if (where.isPresent()) {
      builder.addMethod(override(where.get()).addStatement("return this").build());
    }

    Optional<MethodSpec> sortBy =
        queryRoot.methodSpecs.stream().filter(m -> "sortBy".equals(m.name)).findAny();
    if (sortBy.isPresent()) {
      builder.addMethod(override(sortBy.get()).addStatement("return this").build());
    }
=======
    where.ifPresent(
        methodSpec ->
            builder.addMethod(CodeGen.override(methodSpec).addStatement("return this").build()));

    Optional<MethodSpec> sortBy =
        queryRoot.methodSpecs.stream().filter(m -> "sortBy".equals(m.name)).findAny();
    sortBy.ifPresent(
        methodSpec ->
            builder.addMethod(CodeGen.override(methodSpec).addStatement("return this").build()));
>>>>>>> b8eae2b... Add <field>IsNull(boolean) in the constraint generator. More tests for BoundField. Fixes some issues with imports.

    Optional<MethodSpec> getById =
        queryRoot.methodSpecs.stream().filter(m -> "getById".equals(m.name)).findAny();
    if (getById.isPresent()) {
      Preconditions.checkNotNull(id);
      TypeName entitiesList =
          getUnique(queryRoot.methodSpecs, m -> "get".equals(m.name)).returnType;
      ParameterSpec contentResolver =
<<<<<<< HEAD
          getUnique(getById.get().parameters, p -> p.type.equals(AndroidClasses.CONTENT_RESOLVER));
      ParameterSpec idParam = getUnique(getById.get().parameters, p -> id.getType().equals(p.type));
=======
          CollectionUtils.getUnique(
              getById.get().parameters, p -> p.type.equals(AndroidClasses.CONTENT_RESOLVER));
      ParameterSpec idParam =
          CollectionUtils.getUnique(getById.get().parameters, p -> id.getType().equals(p.type));
>>>>>>> b8eae2b... Add <field>IsNull(boolean) in the constraint generator. More tests for BoundField. Fixes some issues with imports.
      builder.addMethod(
          override(getById.get())
              .addStatement(
                  "$T entities = $N($T.EQ, $N).get($N)",
                  entitiesList,
                  id.getFieldName(),
                  Operator.class,
                  idParam,
                  contentResolver)
              .beginControlFlow("try")
              .addStatement("return entities.size() == 1 ? entities.get(0) : null")
              .nextControlFlow("finally")
              .addStatement("entities.close()")
              .endControlFlow()
              .build());
    }
  }

  private static CodeBlock query(
      String cursorName, ParameterSpec contentResolver, FieldSpec projection) {
    CodeBlock.Builder builder =
        CodeBlock.builder()
            .addStatement("String query = $N.isEmpty() ? null : $N.toString()", args, query)
            .addStatement("String[] args = $N.isEmpty() ? null : new String[$N.size()]", args, args)
            .addStatement(
                "String sortOrder = $N.length() == 0 ? null : $N.toString()", sortOrder, sortOrder)
            .beginControlFlow("if (args != null)")
            .addStatement("args = $N.toArray(args)", args)
            .endControlFlow();
    if (projection != null) {
      builder.addStatement(
          "$T $L = $N.query($N, $N, query, args, sortOrder)",
          AndroidClasses.CURSOR,
          cursorName,
          contentResolver,
          uri,
          projection);
    } else {
      builder.addStatement(
          "$T $L = $N.query($N, new String[] { \"count(*)\" }, query, args, sortOrder)",
          AndroidClasses.CURSOR,
          cursorName,
          contentResolver,
          uri);
    }
    return builder.build();
  }

  private static MethodSpec generateSortCriterion(BoundField boundField) {
    MethodSpec sortBy = Preconditions.checkNotNull(boundField.getSortBy());
    ParameterSpec order =
<<<<<<< HEAD
        getUnique(sortBy.parameters, p -> p.type.equals(ClassName.get(Order.class)));
=======
        CollectionUtils.getUnique(
            sortBy.parameters, p -> p.type.equals(ClassName.get(Order.class)));
>>>>>>> b8eae2b... Add <field>IsNull(boolean) in the constraint generator. More tests for BoundField. Fixes some issues with imports.
    MethodSpec.Builder builder =
        override(sortBy)
            .addStatement(
                "$N.append($S).append(' ').append($N.toString())",
                sortOrder,
                boundField.getColumn(),
                order)
            .addStatement("return this");
    return builder.build();
  }

  private static MethodSpec generateConstraint(BoundField field) {
    MethodSpec constraint = Preconditions.checkNotNull(field.getConstraint());
    ParameterSpec op =
<<<<<<< HEAD
        getUnique(constraint.parameters, p -> p.type.equals(ClassName.get(Operator.class)));
    ParameterSpec value = getUnique(constraint.parameters, p -> p.type.equals(field.getType()));
=======
        CollectionUtils.getUnique(
            constraint.parameters, p -> p.type.equals(ClassName.get(Operator.class)));
    ParameterSpec value =
        CollectionUtils.getUnique(constraint.parameters, p -> p.type.equals(field.getType()));
>>>>>>> b8eae2b... Add <field>IsNull(boolean) in the constraint generator. More tests for BoundField. Fixes some issues with imports.
    MethodSpec.Builder builder =
        override(constraint)
            .addStatement(
                "$N.append($S).append($N.toString()).append('?')", query, field.getColumn(), op);
    if (field.getType().equals(TypeName.get(String.class))) {
      builder.addStatement("$N.add($N)", args, value);
    } else {
      Preconditions.checkState(field.getType().isPrimitive());
      builder.addStatement("$N.add($T.toString($N))", args, field.getType().box(), value);
    }
    return builder.addStatement("return this").build();
  }

  private static MethodSpec generateIsNull(BoundField field) {
    MethodSpec constraint = Preconditions.checkNotNull(field.getIsNull());
    ParameterSpec value =
        CollectionUtils.getUnique(constraint.parameters, p -> p.type.equals(TypeName.BOOLEAN));
    MethodSpec.Builder builder =
        CodeGen.override(constraint)
            .addStatement(
                "$N.append($S).append($N ? \" IS NULL\" : \" IS NOT NULL\")",
                query,
                field.getColumn(),
                value);
    return builder.addStatement("return this").build();
  }
}
