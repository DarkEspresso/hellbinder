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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;
import org.junit.Before;
import org.junit.Test;
import tech.darkespresso.hellbinder.annotations.ContentUri;
/** Tests for {@link Uri}. */
public class UriTest {
  private TypeElement mockEntity;
  private VariableElement mockUriVariableElement;
  private ExecutableElement mockUriExecutableElement;

  @Before
  public void setUp() {
    mockEntity = mock(TypeElement.class);
    Name entityQualifiedName = mock(Name.class);
    when(entityQualifiedName.toString()).thenReturn("tech.darkespresso.Foo");
    Name entitySimpleName = mock(Name.class);
    when(entitySimpleName.toString()).thenReturn("Foo");
    when(mockEntity.getQualifiedName()).thenReturn(entityQualifiedName);
    when(mockEntity.getSimpleName()).thenReturn(entitySimpleName);

    // Sets up a VariableElement like:
    // class Foo {
    //   @ContentUri
    //   public static final android.net.Uri URI = <...>;
    // }
    ContentUri annotation = mock(ContentUri.class);
    DeclaredType uriType = mock(DeclaredType.class);
    when(uriType.toString()).thenReturn("android.net.Uri");

    Name uriFieldName = mock(Name.class);
    when(uriFieldName.toString()).thenReturn("URI");

    mockUriVariableElement = mock(VariableElement.class);
    when(mockUriVariableElement.asType()).thenReturn(uriType);
    when(mockUriVariableElement.getModifiers())
        .thenReturn(ImmutableSet.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL));
    when(mockUriVariableElement.getAnnotation(ContentUri.class)).thenReturn(annotation);
    when(mockUriVariableElement.getEnclosingElement()).thenReturn(mockEntity);
    when(mockUriVariableElement.getSimpleName()).thenReturn(uriFieldName);

    // Sets up an ExecutableElement like:
    // class Foo {
    //   @ContentUri
    //   public static android.net.Uri uri() { <...> };
    // }
    Name uriMethodName = mock(Name.class);
    when(uriMethodName.toString()).thenReturn("uri");
    mockUriExecutableElement = mock(ExecutableElement.class);
    when(mockUriExecutableElement.getModifiers())
        .thenReturn(ImmutableSet.of(Modifier.PUBLIC, Modifier.STATIC));
    when(mockUriExecutableElement.getAnnotation(ContentUri.class)).thenReturn(annotation);
    when(mockUriExecutableElement.getReturnType()).thenReturn(uriType);
    when(mockUriExecutableElement.getEnclosingElement()).thenReturn(mockEntity);
    when(mockUriExecutableElement.getSimpleName()).thenReturn(uriMethodName);
  }

  @Test
  public void constructor_withValidArguments() {
    Messager messager = mock(Messager.class);
    try {
      Uri uri = new Uri(ImmutableList.of(mockUriVariableElement), mockEntity, messager);
      assertEquals(mockUriVariableElement, uri.uriElement);
    } catch (ProcessingException e) {
      fail();
    }

    try {
      Uri uri = new Uri(ImmutableList.of(mockUriExecutableElement), mockEntity, messager);
      assertEquals(mockUriExecutableElement, uri.uriElement);
    } catch (ProcessingException e) {
      fail();
    }
  }

  @Test
  public void constructor_noUriElement() {
    Messager messager = mock(Messager.class);
    try {
      new Uri(ImmutableList.of(), mockEntity, messager);
      fail();
    } catch (ProcessingException e) {
      assertEquals(mockEntity, e.getElement());
      assertEquals(
          "Class does not have a static field or method annotated with @ContentUri.",
          e.getMessage());
    }
  }

  @Test
  public void constructor_moreThanOneUriElement() {
    Messager messager = mock(Messager.class);
    try {
      new Uri(
          ImmutableList.of(mockUriVariableElement, mockUriExecutableElement), mockEntity, messager);
      fail();
    } catch (ProcessingException e) {
      assertEquals(mockEntity, e.getElement());
      assertEquals(
          "Class contains more than 1 element annotated with @ContentUri.", e.getMessage());
    }
  }

  @Test
  public void constructor_nonFinalStaticField() {
    Messager messager = mock(Messager.class);
    when(mockUriVariableElement.getModifiers())
        .thenReturn(ImmutableSet.of(Modifier.PUBLIC, Modifier.STATIC));
    try {
      Uri uri = new Uri(ImmutableList.of(mockUriVariableElement), mockEntity, messager);
      assertEquals(mockUriVariableElement, uri.uriElement);
      verify(messager)
          .printMessage(
              Diagnostic.Kind.ERROR,
              "Fields annotated with " + "@ContentUri must be final.",
              mockUriVariableElement);
    } catch (ProcessingException e) {
      fail();
    }
  }

  @Test
  public void constructor_nonUriTypes() {
    Messager messager = mock(Messager.class);
    DeclaredType nonUriType = mock(DeclaredType.class);
    when(nonUriType.toString()).thenReturn("android.net.NotAnUri");

    when(mockUriVariableElement.asType()).thenReturn(nonUriType);
    try {
      new Uri(ImmutableList.of(mockUriVariableElement), mockEntity, messager);
      fail();
    } catch (ProcessingException e) {
      assertTrue(e instanceof UnsupportedTypeException);
      assertEquals(mockUriVariableElement, e.getElement());
      assertEquals(
          "static field annotated with @interface tech.darkespresso"
              + ".hellbinder.annotations.ContentUri is not of typeName android.net.Uri.",
          e.getMessage());
    }

    when(mockUriExecutableElement.getReturnType()).thenReturn(nonUriType);
    try {
      new Uri(ImmutableList.of(mockUriExecutableElement), mockEntity, messager);
      fail();
    } catch (ProcessingException e) {
      assertTrue(e instanceof UnsupportedTypeException);
      assertEquals(mockUriExecutableElement, e.getElement());
      assertEquals("static method uri's return typeName is not android.net.Uri.", e.getMessage());
    }
  }

  @Test
  public void constructor_nonStaticAndNonPublic() {
    Messager messager = mock(Messager.class);
    when(mockUriVariableElement.getModifiers()).thenReturn(ImmutableSet.of(Modifier.PUBLIC));
    try {
      new Uri(ImmutableList.of(mockUriVariableElement), mockEntity, messager);
      fail();
    } catch (ProcessingException e) {
      assertEquals(mockUriVariableElement, e.getElement());
      assertEquals("@ContentUri must annotate a public static field or method.", e.getMessage());
    }
    when(mockUriVariableElement.getModifiers()).thenReturn(ImmutableSet.of(Modifier.STATIC));
    try {
      new Uri(ImmutableList.of(mockUriVariableElement), mockEntity, messager);
      fail();
    } catch (ProcessingException e) {
      assertEquals(mockUriVariableElement, e.getElement());
      assertEquals("@ContentUri must annotate a public static field or method.", e.getMessage());
    }
  }

  @Test
  public void constructor_nonVariableOrExecutableElementAnnotatedWithContentUri() {
    Messager messager = mock(Messager.class);
    TypeElement typeElement = mock(TypeElement.class);
    when(typeElement.getAnnotation(ContentUri.class)).thenReturn(mock(ContentUri.class));
    when(typeElement.getModifiers()).thenReturn(ImmutableSet.of(Modifier.STATIC, Modifier.PUBLIC));
    try {
      new Uri(ImmutableList.of(typeElement), mockEntity, messager);
      fail();
    } catch (ProcessingException e) {
      assertEquals(typeElement, e.getElement());
      assertEquals(
          "Annotation @ContentUri can only be used for fields or methods.", e.getMessage());
    }
  }

  @Test
  public void uriReference() {
    Messager messager = mock(Messager.class);
    try {
      Uri uri = new Uri(ImmutableList.of(mockUriVariableElement), mockEntity, messager);
      assertEquals("Foo.URI", uri.getLiteralExpression());
    } catch (ProcessingException e) {
      fail();
    }

    try {
      Uri uri = new Uri(ImmutableList.of(mockUriExecutableElement), mockEntity, messager);
      assertEquals("Foo.uri()", uri.getLiteralExpression());
    } catch (ProcessingException e) {
      fail();
    }
  }

  @Test
  public void needsParameters() {
    Messager messager = mock(Messager.class);
    try {
      Uri uri = new Uri(ImmutableList.of(mockUriVariableElement), mockEntity, messager);
      assertFalse(uri.needsParameters());
    } catch (ProcessingException e) {
      fail();
    }

    when(mockUriExecutableElement.getParameters()).thenReturn(ImmutableList.of());
    try {
      Uri uri = new Uri(ImmutableList.of(mockUriExecutableElement), mockEntity, messager);
      assertFalse(uri.needsParameters());
    } catch (ProcessingException e) {
      fail();
    }

    VariableElement param = mock(VariableElement.class);
    List<VariableElement> params = ImmutableList.of(param);
    when(mockUriExecutableElement.getParameters()).thenAnswer(invocation -> params);
    try {
      Uri uri = new Uri(ImmutableList.of(mockUriExecutableElement), mockEntity, messager);
      assertTrue(uri.needsParameters());
    } catch (ProcessingException e) {
      fail();
    }
  }
}
