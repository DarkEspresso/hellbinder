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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static tech.darkespresso.hellbinder.compiler.utils.CollectionUtils.uniqueOrNull;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.NoSuchElementException;
import org.junit.Test;

/** Tests for {@link CollectionUtils}. */
public class CollectionUtilsTest {
  @Test
  public void getUnique_hasUniqueElement() {
    Collection<Integer> collection = ImmutableList.of(1, 2, 3, 4, 5);
    try {
      int unique = CollectionUtils.getUnique(collection, i -> i == 3);
      assertEquals(3, unique);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void getUnique_noSuchElement() {
    Collection<Integer> collection = ImmutableList.of(1, 2, 3, 4, 5);
    try {
      CollectionUtils.getUnique(collection, i -> i == 6);
      fail();
    } catch (Exception e) {
      assertTrue(e instanceof NoSuchElementException);
    }
  }

  @Test
  public void getUnique_duplicateElement() {
    Collection<Integer> collection = ImmutableList.of(1, 3, 3, 4, 5);
    try {
      CollectionUtils.getUnique(collection, i -> i == 3);
      fail();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
  }

  @Test
  public void uniqueOrNull_hasUniqueElement() {
    Collection<String> collection = ImmutableList.of("Foo", "Bar", "Foobar");
    String actual = collection.stream().filter("Bar"::equals).collect(uniqueOrNull());
    assertEquals("Bar", actual);

    String actualParallel =
        collection.parallelStream().filter("Foobar"::equals).collect(uniqueOrNull());
    assertEquals("Foobar", actualParallel);
  }

  @Test
  public void uniqueOrNull_noElement() {
    Collection<String> collection = ImmutableList.of("Foo", "Bar", "Foobar");
    String actual = collection.stream().filter("ooF"::equals).collect(uniqueOrNull());
    assertNull(actual);
  }

  @Test
  public void uniqueOrNull_returnsNullWhenNotUnique() {
    Collection<String> collection = ImmutableList.of("Foo", "Bar", "Foo", "Foobar");
    String actual = collection.stream().filter("Foo"::equals).collect(uniqueOrNull());
    assertNull(actual);

    String actualParallel =
        collection.parallelStream().filter("Foo"::equals).collect(uniqueOrNull());
    assertNull(actualParallel);
  }
}
