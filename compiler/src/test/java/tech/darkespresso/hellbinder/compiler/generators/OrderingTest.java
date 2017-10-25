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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import org.junit.Test;

/** Tests for {@link Ordering} */
public class OrderingTest {
  @Test
  public void generate() {
    TypeName entityType = ClassName.get("", "Foo");

    TypeSpec actual = Ordering.generate(entityType);

    assertEquals(1, actual.methodSpecs.size());
    MethodSpec thenBy =
        MethodSpec.methodBuilder("thenBy")
            .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
            .returns(ClassName.get("", "OrderBuilder"))
            .build();
    assertTrue(actual.methodSpecs.stream().anyMatch(thenBy::equals));

    TypeName superInterface =
        ParameterizedTypeName.get(
            ClassName.get("tech.darkespresso.hellbinder", "QueryExecutor"),
            ClassName.get("", "Foo"),
            ClassName.get("android.content", "ContentResolver"));
    assertEquals(1, actual.superinterfaces.size());
    assertTrue(actual.superinterfaces.stream().anyMatch(superInterface::equals));
  }

  @Test
  public void generate_nullEntityType() {
    try {
      Ordering.generate(null);
      fail();
    } catch (NullPointerException e) {
      // expected.
    }
  }
}
