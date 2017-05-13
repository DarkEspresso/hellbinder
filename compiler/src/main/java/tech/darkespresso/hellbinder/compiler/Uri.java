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

import com.google.common.base.Preconditions;
import com.google.common.collect.MoreCollectors;
import java.util.List;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import tech.darkespresso.hellbinder.annotations.ContentUri;
/**
 * Finds the static field or method annotated with {@link ContentUri} of a class annotated with
 * {@link Entity}.
 */
public final class Uri {
  /** The {@link Element} annotated with {@link ContentUri}. */
  @Nonnull final Element uriElement;

  public Uri(@Nonnull List<? extends Element> elements, TypeElement entity, Messager messager)
      throws ProcessingException {
    elements = Preconditions.checkNotNull(elements);
    Preconditions.checkArgument(
        elements.stream().allMatch(e -> e.getAnnotation(ContentUri.class) != null));
    try {
      uriElement =
          validateUriElement(elements.stream().collect(MoreCollectors.onlyElement()), messager);
    } catch (NoSuchElementException e) {
      throw new ProcessingException(
          "Class does not have a static field or method annotated with @ContentUri.", entity, e);
    } catch (IllegalArgumentException e) {
      if (elements.size() > 1) {
        throw new ProcessingException(
            "Class contains more than 1 element annotated with @ContentUri.", entity, e);
      } else {
        throw e;
      }
    }
  }

  /**
   * @return {@code true} if the Uri does not need depend on some parameter, {@code false}
   *     otherwise.
   */
  public boolean needsParameters() {
    if (uriElement instanceof VariableElement) {
      return false;
    } else {
      ExecutableElement executableElement = (ExecutableElement) uriElement;
      return !executableElement.getParameters().isEmpty();
    }
  }

  public Element getElement() {
    return uriElement;
  }

  public String getLiteralExpression() {
    Preconditions.checkState(!needsParameters());
    return String.format(
        "%s.%s%s",
        uriElement.getEnclosingElement().getSimpleName(),
        uriElement.getSimpleName(),
        uriElement instanceof ExecutableElement ? "()" : "");
  }

  private Element validateUriElement(@Nonnull Element element, Messager messager)
      throws ProcessingException {
    element = Preconditions.checkNotNull(element);
    if (!element.getModifiers().contains(Modifier.PUBLIC)
        || !element.getModifiers().contains(Modifier.STATIC)) {
      throw new ProcessingException(
          "@ContentUri must annotate a public static field or method.", element);
    }

    if (element instanceof VariableElement) {
      if (!element.getModifiers().contains(Modifier.FINAL)) {
        messager.printMessage(
            Diagnostic.Kind.ERROR, "Fields annotated with @ContentUri must be final.", element);
      }
      VariableElement variableElement = (VariableElement) element;
      if (!AndroidClasses.URI.reflectionName().equals(variableElement.asType().toString())) {
        throw new UnsupportedTypeException(
            String.format(
                "static field annotated with @%s is not of typeName %s.",
                ContentUri.class, AndroidClasses.URI.reflectionName()),
            variableElement);
      }
      return variableElement;
    } else if (element instanceof ExecutableElement) {
      ExecutableElement executableElement = (ExecutableElement) element;
      if (!AndroidClasses.URI
          .reflectionName()
          .equals(executableElement.getReturnType().toString())) {
        throw new UnsupportedTypeException(
            String.format(
                "static method %s's return typeName is not %s.",
                executableElement.getSimpleName(), AndroidClasses.URI.reflectionName()),
            executableElement);
      }
      return executableElement;
    } else {
      throw new ProcessingException(
          "Annotation @ContentUri can only be used for fields or methods.", element);
    }
  }
}
