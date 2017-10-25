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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.darkespresso.hellbinder.compiler.utils.CodeGenTest.CursorTestParams.of;
import static tech.darkespresso.hellbinder.compiler.utils.CodeGenTest.CursorTestParams.ofString;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import javax.annotation.Nonnull;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.junit.Test;
import tech.darkespresso.hellbinder.compiler.UnsupportedTypeException;

/** Tests for {@link CodeGen}. */
public class CodeGenTest {

  @Test
  public void implementStatic_public() {
    MethodSpec argument =
        MethodSpec.methodBuilder("foo")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.BOOLEAN, "bar")
            .addAnnotation(Nonnull.class)
            .returns(TypeName.INT)
            .build();
    MethodSpec.Builder result = CodeGen.implementStatic(argument);
    MethodSpec expected =
        MethodSpec.methodBuilder("foo")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(TypeName.BOOLEAN, "bar")
            .addAnnotation(Nonnull.class)
            .returns(TypeName.INT)
            .build();
    assertEquals(expected, result.build());
  }

  @Test
  public void implementStatic_protected() {
    MethodSpec argument =
        MethodSpec.methodBuilder("foo")
            .addModifiers(Modifier.PROTECTED)
            .addParameter(TypeName.BOOLEAN, "bar")
            .addAnnotation(Nonnull.class)
            .returns(TypeName.INT)
            .build();
    MethodSpec.Builder result = CodeGen.implementStatic(argument);
    MethodSpec expected =
        MethodSpec.methodBuilder("foo")
            .addModifiers(Modifier.PROTECTED, Modifier.STATIC)
            .addParameter(TypeName.BOOLEAN, "bar")
            .addAnnotation(Nonnull.class)
            .returns(TypeName.INT)
            .build();
    assertEquals(expected, result.build());
  }

  @Test
  public void implementStatic_removesAbstract() {
    MethodSpec argument =
        MethodSpec.methodBuilder("foo")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(TypeName.BOOLEAN, "bar")
            .addAnnotation(Nonnull.class)
            .returns(TypeName.INT)
            .build();
    MethodSpec.Builder result = CodeGen.implementStatic(argument);
    MethodSpec expected =
        MethodSpec.methodBuilder("foo")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(TypeName.BOOLEAN, "bar")
            .addAnnotation(Nonnull.class)
            .returns(TypeName.INT)
            .build();
    assertEquals(expected, result.build());
  }

  @Test
  public void implementStatic_failsIfOverrideAnnotationIsPresent() {
    MethodSpec argument =
        MethodSpec.methodBuilder("foo")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.BOOLEAN, "bar")
            .addAnnotation(Override.class)
            .returns(TypeName.INT)
            .build();
    try {
      CodeGen.implementStatic(argument);
      fail();
    } catch (IllegalArgumentException e) {
      // expected.
    }
  }

  @Test
  public void implementStatic_failsIfMethodIsPrivate() {
    MethodSpec argument =
        MethodSpec.methodBuilder("foo")
            .addModifiers(Modifier.PRIVATE)
            .addParameter(TypeName.BOOLEAN, "bar")
            .addAnnotation(Override.class)
            .returns(TypeName.INT)
            .build();
    try {
      CodeGen.implementStatic(argument);
      fail();
    } catch (IllegalArgumentException e) {
      // expected.
    }
  }

  @Test
  public void override_public() {
    MethodSpec argument =
        MethodSpec.methodBuilder("foo")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.BOOLEAN, "bar")
            .addAnnotation(Nonnull.class)
            .returns(TypeName.INT)
            .build();
    MethodSpec.Builder result = CodeGen.override(argument);
    MethodSpec expected =
        MethodSpec.methodBuilder("foo")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addParameter(TypeName.BOOLEAN, "bar")
            .addAnnotation(Nonnull.class)
            .addAnnotation(Override.class)
            .returns(TypeName.INT)
            .build();
    assertEquals(expected, result.build());
  }

  @Test
  public void override_protected() {
    MethodSpec argument =
        MethodSpec.methodBuilder("foo")
            .addModifiers(Modifier.PROTECTED)
            .addParameter(TypeName.BOOLEAN, "bar")
            .addAnnotation(Nonnull.class)
            .returns(TypeName.INT)
            .build();
    MethodSpec.Builder result = CodeGen.override(argument);
    MethodSpec expected =
        MethodSpec.methodBuilder("foo")
            .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
            .addParameter(TypeName.BOOLEAN, "bar")
            .addAnnotation(Nonnull.class)
            .addAnnotation(Override.class)
            .returns(TypeName.INT)
            .build();
    assertEquals(expected, result.build());
  }

  @Test
  public void override_removesAbstract() {
    MethodSpec argument =
        MethodSpec.methodBuilder("foo")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(TypeName.BOOLEAN, "bar")
            .addAnnotation(Nonnull.class)
            .returns(TypeName.INT)
            .build();
    MethodSpec.Builder result = CodeGen.override(argument);
    MethodSpec expected =
        MethodSpec.methodBuilder("foo")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addParameter(TypeName.BOOLEAN, "bar")
            .addAnnotation(Nonnull.class)
            .addAnnotation(Override.class)
            .returns(TypeName.INT)
            .build();
    assertEquals(expected, result.build());
  }

  @Test
  public void implementStatic_failsIfStatic() {
    MethodSpec argument =
        MethodSpec.methodBuilder("foo")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(TypeName.BOOLEAN, "bar")
            .returns(TypeName.INT)
            .build();
    try {
      CodeGen.override(argument);
      fail();
    } catch (IllegalArgumentException e) {
      // expected.
    }
  }

  @Test
  public void override_failsIfMethodIsPrivate() {
    MethodSpec argument =
        MethodSpec.methodBuilder("foo")
            .addModifiers(Modifier.PRIVATE)
            .addParameter(TypeName.BOOLEAN, "bar")
            .addAnnotation(Override.class)
            .returns(TypeName.INT)
            .build();
    try {
      CodeGen.override(argument);
      fail();
    } catch (IllegalArgumentException e) {
      // expected.
    }
  }

  @Test
  public void cursorGetterFor() {
    CursorTestParams[] testParamsArray = {
      of(TypeKind.INT, "getInt"),
      of(TypeKind.LONG, "getLong"),
      of(TypeKind.SHORT, "getShort"),
      of(TypeKind.FLOAT, "getFloat"),
      of(TypeKind.DOUBLE, "getDouble"),
      ofString()
    };
    for (CursorTestParams testParams : testParamsArray) {
      VariableElement mockElement = mock(VariableElement.class);
      when(mockElement.asType()).thenReturn(testParams.typeMirror);
      try {
        CharSequence getter = CodeGen.cursorGetterFor(mockElement);
        assertEquals(testParams.expected, getter);
      } catch (UnsupportedTypeException e) {
        fail();
      }
    }
  }

  @Test
  public void cursorGetterFor_unsupportedType() {
    VariableElement mockElement = mock(VariableElement.class);
    TypeMirror typeMirror = mock(TypeMirror.class);
    when(typeMirror.getKind()).thenReturn(TypeKind.OTHER);
    when(mockElement.asType()).thenReturn(typeMirror);
    try {
      CodeGen.cursorGetterFor(mockElement);
      fail();
    } catch (UnsupportedTypeException e) {
      // expected.
    }
  }

  static final class CursorTestParams {
    final TypeMirror typeMirror;
    final CharSequence expected;

    CursorTestParams(TypeMirror typeMirror, String expected) {
      this.typeMirror = typeMirror;
      this.expected = expected;
    }

    static CursorTestParams of(TypeKind kind, String expected) {
      CursorTestParams result = new CursorTestParams(mock(TypeMirror.class), expected);
      when(result.typeMirror.getKind()).thenReturn(kind);
      return result;
    }

    static CursorTestParams ofString() {
      CursorTestParams result = new CursorTestParams(mock(TypeMirror.class), "getString");
      when(result.typeMirror.toString()).thenReturn(String.class.getCanonicalName());
      return result;
    }
  }
}
