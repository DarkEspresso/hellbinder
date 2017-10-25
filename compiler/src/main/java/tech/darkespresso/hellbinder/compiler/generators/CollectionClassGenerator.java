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

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static javax.tools.Diagnostic.Kind.ERROR;
import static tech.darkespresso.hellbinder.compiler.utils.CodeGen.cursorGetterFor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import tech.darkespresso.hellbinder.Operator;
import tech.darkespresso.hellbinder.annotations.ContentProviderEntity;
import tech.darkespresso.hellbinder.annotations.ContentUri;
import tech.darkespresso.hellbinder.compiler.AndroidClasses;
import tech.darkespresso.hellbinder.compiler.BoundField;
import tech.darkespresso.hellbinder.compiler.Entity;
import tech.darkespresso.hellbinder.compiler.ProcessingException;
import tech.darkespresso.hellbinder.compiler.UnsupportedTypeException;
import tech.darkespresso.hellbinder.compiler.Uri;
import tech.darkespresso.hellbinder.compiler.utils.CodeGen;
import tech.darkespresso.hellbinder.compiler.utils.CollectionUtils;

/**
 * Contains the methods to generate the class that provides the methods to retrieve entities
 * from a content provider.
 *
 * <p>The name of the generated class is given by the value specified on
 * the {@link ContentProviderEntity ContentProviderEntity} annotation for a given entity class.
 *
 * <p>For example, suppose that a class name {@code com.foobar.Contact} is annotated with {@code
 * @literal @ContentProviderEntity("Contacts")}:
 * <pre>{@code
 * package com.foobar;
 * ...
 *@literal @ContentProivderEntity("Contacts")
 * public class Contact {
 *    @literal @ContentUri public static final Uri URI = ContactsContract.Contacts.CONTENT_URI;
 *
 *    @literal @Id
 *    @literal @Column(ContactsContract.Contacts._ID)
 *     public long id;
 *
 *    @literal @SortCriterion
 *    @literal @Column(ContactsContract.Contacts.DISPLAY_NAME)
 *     public String name;
 *
 *    @literal @Constraint
 *    @literal @Column(ContactsContract.Contacts.HAS_PHONE_NUMBER)
 *     public int hasPhoneNumber;
 *
 *    @literal @Column(ContactsContract.Contacts.PHOTO_ID)
 *     public String photoId;
 *     ...
 * }}</pre>
 *
 * The generated class will be:
 *
 * <pre>{@code
 * package com.foobar;
 * ...
 * public class Contacts {
 *   ...
 *   public static QueryBuilder where() { ... }
 *
 *   public static OrderBuilder sortBy() { ... }
 *
 *   public static CloseableList<Contact> get(ContentResolver contentResolver) { ... }
 *
 *   public static Contact getById(ContentResolver contentResolver, long id) { ... }
 * }}</pre>
 */
public class CollectionClassGenerator {

  private CollectionClassGenerator() {
    throw new UnsupportedOperationException();
  }

  @VisibleForTesting
  public static TypeSpec generate(@Nonnull Entity entity, @Nonnull Messager messager) {
    entity = Preconditions.checkNotNull(entity);
    messager = Preconditions.checkNotNull(messager);
    TypeSpec.Builder builder =
        TypeSpec.classBuilder(entity.getCollectionClassName())
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

    MethodSpec constructor =
        MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .addStatement("throw new $T()", UnsupportedOperationException.class)
            .build();
    builder
        .addMethod(constructor)
        .addField(entity.getProjection())
        .addMethod(entity.getBindMethod())
        .addType(EntityList.generate(entity.getTypeName(), entity.getBindMethod()));

    final List<BoundField> fields = entity.getFields();
    addRequiredInterfaces(entity.getTypeName(), builder, fields);

    Uri uri;
    try {
      uri =
          new Uri(entity.getElementsAnnotatedWith(ContentUri.class), entity.getElement(), messager);
    } catch (ProcessingException e) {
      messager.printMessage(ERROR, e.getMessage(), e.getElement());
      return builder.build();
    }

    TypeSpec queryRoot = QueryRoot.generate(entity.getTypeName(), fields);
    if (uri.needsParameters()) {
      builder.addType(queryRoot);
      builder.addMethod(generateWithUriParams(uri.getElement()));
      builder.addType(
          QueryBuilderImpl.generate(
              entity.getTypeName(), entity.getProjection(), fields, queryRoot));
    } else {
      addRequiredRootMethods(builder, queryRoot, fields, uri.getLiteralExpression());
      addStaticRootMethodsHelpers(builder, queryRoot, fields, uri.getLiteralExpression());
      builder.addType(
          QueryBuilderImpl.generate(entity.getTypeName(), entity.getProjection(), fields, null));
    }

    return builder.build();
  }

  @VisibleForTesting
  public static MethodSpec generateBind(
      @Nonnull TypeName entityType, @Nonnull List<BoundField> fields, @Nonnull Messager messager) {
    entityType = Preconditions.checkNotNull(entityType);
    fields = Preconditions.checkNotNull(fields);
    messager = Preconditions.checkNotNull(messager);
    MethodSpec.Builder bindMethod =
        MethodSpec.methodBuilder("bind")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .addParameter(entityType, "entity")
            .addParameter(AndroidClasses.CURSOR, "cursor");
    for (int i = 0; i < fields.size(); ++i) {
      BoundField boundField = fields.get(i);
      VariableElement field = boundField.getField();
      try {
        bindMethod.addStatement(
            "entity.$L = cursor.$L($L)", boundField.getFieldName(), cursorGetterFor(field), i);
      } catch (UnsupportedTypeException e) {
        messager.printMessage(ERROR, e.getMessage(), field);
      }
    }
    return bindMethod.build();
  }

  @SuppressWarnings("WeakerAccess")
  @VisibleForTesting
  static void addRequiredInterfaces(
      @Nonnull TypeName entityType,
      @Nonnull TypeSpec.Builder builder,
      @Nonnull List<BoundField> fields) {
    boolean hasSortCriteria = fields.stream().anyMatch(BoundField::canBeUsedForSorting);
    if (fields.stream().anyMatch(BoundField::canBeConstrained)) {
      TypeSpec queryBuilder = QueryBuilder.generate(fields);
      builder.addType(queryBuilder);
      builder.addType(Constraining.generate(entityType, hasSortCriteria));
    }
    if (hasSortCriteria) {
      TypeSpec orderBuilder = OrderBuilder.generate(fields);
      builder.addType(orderBuilder);
      builder.addType(Ordering.generate(entityType));
    }
  }

  @SuppressWarnings("WeakerAccess")
  @VisibleForTesting
  static void addRequiredRootMethods(
      @Nonnull TypeSpec.Builder builder,
      @Nonnull TypeSpec queryRoot,
      @Nonnull List<BoundField> fields,
      @Nonnull String uri) {
    if (fields.stream().anyMatch(BoundField::canBeConstrained)) {
      MethodSpec where =
          CollectionUtils.getUnique(queryRoot.methodSpecs, m -> "where".equals(m.name));
      builder.addMethod(
          CodeGen.implementStatic(where)
              .addStatement("return new $T($L)", QueryBuilderImpl.NAME, uri)
              .build());
    }
    if (fields.stream().anyMatch(BoundField::canBeUsedForSorting)) {
      MethodSpec sortBy =
          CollectionUtils.getUnique(queryRoot.methodSpecs, m -> "sortBy".equals(m.name));
      builder.addMethod(
          CodeGen.implementStatic(sortBy)
              .addStatement("return new $T($L)", QueryBuilderImpl.NAME, uri)
              .build());
    }
  }

  @SuppressWarnings("WeakerAccess")
  @VisibleForTesting
  static void addStaticRootMethodsHelpers(
      @Nonnull TypeSpec.Builder builder,
      @Nonnull TypeSpec queryRoot,
      @Nonnull List<BoundField> fields,
      @Nonnull String uri) {
    MethodSpec get = CollectionUtils.getUnique(queryRoot.methodSpecs, m -> "get".equals(m.name));
    ParameterSpec contentResolver =
        CollectionUtils.getUnique(
            get.parameters, p -> AndroidClasses.CONTENT_RESOLVER.equals(p.type));
    builder.addMethod(
        CodeGen.implementStatic(get)
            .addStatement("return new $T($L).get($N)", QueryBuilderImpl.NAME, uri, contentResolver)
            .build());

    MethodSpec count =
        CollectionUtils.getUnique(queryRoot.methodSpecs, m -> "count".equals(m.name));
    builder.addMethod(
        CodeGen.implementStatic(count)
            .addStatement(
                "return new $T($L).count($N)", QueryBuilderImpl.NAME, uri, contentResolver)
            .build());

    BoundField id =
        fields.stream().filter(BoundField::isId).collect(CollectionUtils.uniqueOrNull());
    if (id != null) {
      MethodSpec getById =
          CollectionUtils.getUnique(queryRoot.methodSpecs, m -> "getById".equals(m.name));
      contentResolver =
          CollectionUtils.getUnique(
              getById.parameters, p -> AndroidClasses.CONTENT_RESOLVER.equals(p.type));
      ParameterSpec idParam =
          CollectionUtils.getUnique(getById.parameters, p -> id.getType().equals(p.type));
      builder.addMethod(
          CodeGen.implementStatic(getById)
              .addStatement(
                  "$T entities = where().$L($T.EQ, $N).get($N)",
                  get.returnType,
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

  @SuppressWarnings("WeakerAccess")
  @VisibleForTesting
  static MethodSpec generateWithUriParams(@Nonnull Element uriElement) {
    uriElement = Preconditions.checkNotNull(uriElement);
    Preconditions.checkArgument(uriElement instanceof ExecutableElement);
    ExecutableElement uriMethod = (ExecutableElement) uriElement;
    String uriRef =
        String.format(
            "%s.%s", uriElement.getEnclosingElement().getSimpleName(), uriElement.getSimpleName());
    List<ParameterSpec> params =
        uriMethod.getParameters().stream().map(ParameterSpec::get).collect(toList());
    return MethodSpec.methodBuilder("withUriParams")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .returns(QueryRoot.NAME)
        .addParameters(params)
        .addCode("return new $T($L", QueryBuilderImpl.NAME, uriRef)
        .addCode(params.stream().map(p -> "$N").collect(joining(",", "(", ")")), params.toArray())
        .addStatement(")")
        .build();
  }
}
