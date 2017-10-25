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

package tech.darkespresso.hellbinder.compiler.utils;

import static java.util.stream.Collectors.toList;

import com.google.common.base.Preconditions;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import tech.darkespresso.hellbinder.compiler.UnsupportedTypeException;

/** Contains methods to aid code generation. */
public final class CodeGen {
  private static final AnnotationSpec OVERRIDE = AnnotationSpec.builder(Override.class).build();

  private CodeGen() {
    throw new UnsupportedOperationException();
  }

  /**
   * Creates a {@link MethodSpec.Builder} with the same signature as {@code method}, but static.
   *
   * @param method the method to implement as static.
   * @return a {@code MethodSpec.Builder} with the signature of the static resulting method.
   */
  public static MethodSpec.Builder implementStatic(MethodSpec method) {
    Preconditions.checkArgument(
        method.hasModifier(Modifier.PUBLIC) || method.hasModifier(Modifier.PROTECTED));
    Preconditions.checkArgument(
        !method.annotations.contains(AnnotationSpec.builder(Override.class).build()));
    if (!method.hasModifier(Modifier.ABSTRACT)) {
      return method.toBuilder().addModifiers(Modifier.STATIC);
    } else {
      return MethodSpec.methodBuilder(method.name)
          .addModifiers(
              method.modifiers.stream().filter(m -> m != Modifier.ABSTRACT).collect(toList()))
          .addModifiers(Modifier.STATIC)
          .addParameters(method.parameters)
          .addAnnotations(method.annotations)
          .addExceptions(method.exceptions)
          .returns(method.returnType);
    }
  }

  /**
   * Creates a {@link MethodSpec.Builder} with the same signature as {@code method}, minus the
   * modifier abstract (if present), but final and with the {@link Override @Override} annotation.
   *
   * @param method the method to override.
   * @return a {@code MethodSpec.Builder} with the signature of the overridden method.
   */
  public static MethodSpec.Builder override(MethodSpec method) {
    Preconditions.checkArgument(
        method.hasModifier(Modifier.PUBLIC) || method.hasModifier(Modifier.PROTECTED));
    Preconditions.checkArgument(
        !method.hasModifier(Modifier.FINAL) && !method.hasModifier(Modifier.STATIC));
    MethodSpec.Builder builder;
    if (!method.hasModifier(Modifier.ABSTRACT)) {
      builder = method.toBuilder().addModifiers(Modifier.FINAL);
    } else {
      builder =
          MethodSpec.methodBuilder(method.name)
              .addModifiers(
                  method.modifiers.stream().filter(m -> m != Modifier.ABSTRACT).collect(toList()))
              .addModifiers(Modifier.FINAL)
              .addParameters(method.parameters)
              .addAnnotations(method.annotations)
              .addExceptions(method.exceptions)
              .returns(method.returnType);
    }
    if (!method.annotations.contains(OVERRIDE)) {
      builder.addAnnotation(Override.class);
    }
    return builder;
  }

  public static CharSequence cursorGetterFor(VariableElement field)
      throws UnsupportedTypeException {
    TypeMirror type = field.asType();
    if (String.class.getCanonicalName().equals(type.toString())) {
      return "getString";
    }
    switch (type.getKind()) {
      case INT:
        return "getInt";
      case LONG:
        return "getLong";
      case SHORT:
        return "getShort";
      case FLOAT:
        return "getFloat";
      case DOUBLE:
        return "getDouble";
      default:
        throw new UnsupportedTypeException(
            "Field "
                + field.getSimpleName()
                + " is of typeName "
                + type
                + ", which is not supported.",
            field);
    }
  }
}
