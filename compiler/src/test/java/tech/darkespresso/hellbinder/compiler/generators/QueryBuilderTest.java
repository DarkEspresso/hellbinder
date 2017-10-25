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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import javax.lang.model.element.Modifier;
import org.junit.Test;
import tech.darkespresso.hellbinder.compiler.BoundField;

/** Tests for {@link QueryBuilder} */
public class QueryBuilderTest {
  @Test
  public void generate() {
    // Set up two Fields like:
    // ...
    //   @Constraint
    //   @Column("_bar")
    //   public long bar;
    //
    //   @Column("_foo")
    //   public String foo;
    BoundField bar = mock(BoundField.class);
    BoundField foo = mock(BoundField.class);

    when(bar.getConstraint())
        .thenReturn(
            MethodSpec.methodBuilder("bar")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(ClassName.get("tech.darkespresso.hellbinder", "Operator"), "op")
                .addParameter(TypeName.LONG, "value")
                .returns(Constraining.NAME)
                .build());
    when(bar.canBeConstrained()).thenReturn(true);
    when(foo.getConstraint()).thenReturn(null);
    List<BoundField> fields = ImmutableList.of(foo, bar);

    TypeSpec actual = QueryBuilder.generate(fields);

    TypeSpec expected =
        TypeSpec.interfaceBuilder(QueryBuilder.NAME)
            .addModifiers(Modifier.PUBLIC)
            .addMethod(
                MethodSpec.methodBuilder("bar")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addParameter(ClassName.get("tech.darkespresso.hellbinder", "Operator"), "op")
                    .addParameter(TypeName.LONG, "value")
                    .returns(Constraining.NAME)
                    .build())
            .build();

    assertEquals(expected, actual);
  }

  @Test
  public void generate_nullFields() {
    try {
      QueryBuilder.generate(null);
      fail();
    } catch (NullPointerException e) {
      // expected.
    }
  }
}
