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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.joining;
import static javax.tools.Diagnostic.Kind.ERROR;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import tech.darkespresso.hellbinder.annotations.Column;
import tech.darkespresso.hellbinder.annotations.ContentProviderEntity;
import tech.darkespresso.hellbinder.annotations.Id;
import tech.darkespresso.hellbinder.compiler.generators.CollectionClassGenerator;

/** Wraps a class annotated by {@link ContentProviderEntity}. */
public class Entity {
  private final TypeName typeName;
  private final List<BoundField> fields;
  private final FieldSpec projection;
  private final MethodSpec bind;
  private final TypeElement element;

  Entity(TypeElement element, Types types, Messager messager) throws ProcessingException {
    if (element.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
      throw new ProcessingException(
          String.format("Nested classes cannot be annotated with @%s", ContentProviderEntity.class),
          element);
    }
    this.element = element;
    typeName = TypeName.get(element.asType());
    fields = extractColumnFields(messager, types);
    projection =
        FieldSpec.builder(String[].class, "PROJECTION")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer(
                fields
                    .stream()
                    .map(BoundField::getColumn)
                    .collect(joining("\", \"", "{ \"", "\" }")))
            .build();
    bind = CollectionClassGenerator.generateBind(typeName, fields, messager);
    if (fields.stream().filter(BoundField::isId).count() > 1) {
      messager.printMessage(ERROR, "more than 1 field annotated with " + Id.class, element);
    }
  }

  public ClassName getCollectionClassName() {
    PackageElement packageElement = (PackageElement) element.getEnclosingElement();
    return ClassName.get(
        packageElement.getQualifiedName().toString(),
        element.getAnnotation(ContentProviderEntity.class).value());
  }

  public FieldSpec getProjection() {
    return projection;
  }

  public MethodSpec getBindMethod() {
    return bind;
  }

  public TypeName getTypeName() {
    return typeName;
  }

  public List<BoundField> getFields() {
    return fields;
  }

  public TypeElement getElement() {
    return element;
  }

  public List<Element> getElementsAnnotatedWith(Class<? extends Annotation> annotation) {
    return element
        .getEnclosedElements()
        .stream()
        .filter(element -> element.getAnnotation(annotation) != null)
        .collect(Collectors.toList());
  }

  private List<BoundField> extractColumnFields(Messager messager, Types types) {
    final ArrayDeque<TypeElement> hierarchy = new ArrayDeque<>();
    TypeElement currentClass = element;
    do {
      hierarchy.push(currentClass);
      if (currentClass.getSuperclass().getKind() == TypeKind.NONE) {
        break;
      }
      currentClass = (TypeElement) types.asElement(currentClass.getSuperclass());
    } while (currentClass.getAnnotation(ContentProviderEntity.class) != null);

    final BiMap<VariableElement, String> fieldColumnMap = HashBiMap.create();
    while (!hierarchy.isEmpty()) {
      currentClass = hierarchy.pop();
      for (VariableElement element : ElementFilter.fieldsIn(currentClass.getEnclosedElements())) {
        if (!BoundField.isValid(element, messager)) {
          continue;
        }
        String column = element.getAnnotation(Column.class).value();
        if (fieldColumnMap.containsValue(column)) {
          VariableElement field = fieldColumnMap.inverse().get(column);
          messager.printMessage(
              ERROR,
              String.format("Column %s already mapped to field @%s", column, field),
              element);
          continue;
        }
        fieldColumnMap.put(element, column);
      }
    }
    return fieldColumnMap.keySet().stream().map(BoundField::new).collect(toImmutableList());
  }
}
