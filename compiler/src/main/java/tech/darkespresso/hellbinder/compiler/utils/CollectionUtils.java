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

import com.google.common.base.Preconditions;
import com.google.common.collect.MoreCollectors;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
import javax.annotation.Nonnull;

/** A collection of static utility methods. */
public final class CollectionUtils {
  private static final Collector<Object, ?, Object> UNIQUE_OR_NULL =
      Collector.of(
          UniqueState::new,
          UniqueState::accumulate,
          UniqueState::combine,
          UniqueState::finish,
          Characteristics.UNORDERED);

  private CollectionUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * Obtains the only element in the given collection that satisfies the predicate.
   *
   * @param collection a {@link Collection}.
   * @param predicate a {@link Predicate} to be matched.
   * @return the only element that satisfies the predicate.
   * @throws IllegalArgumentException if more than one element satisfies the predicate.
   * @throws NoSuchElementException if no element in the collection satisfies the predicate.
   */
  public static <T> T getUnique(
      @Nonnull Collection<T> collection, @Nonnull Predicate<T> predicate) {
    collection = Preconditions.checkNotNull(collection);
    predicate = Preconditions.checkNotNull(predicate);
    return collection.stream().filter(predicate).collect(MoreCollectors.onlyElement());
  }

  /**
   * Collects a unique element in the stream, or {@link null} if either no such element exists, or
   * there are more than one.
   */
  @SuppressWarnings("unchecked")
  public static <T> Collector<T, ?, T> uniqueOrNull() {
    return (Collector) UNIQUE_OR_NULL;
  }

  private static class UniqueState {
    boolean duplicate = false;
    private Object element;

    void accumulate(Object a) {
      if (element == null) {
        element = a;
      } else {
        duplicate = true;
      }
    }

    UniqueState combine(UniqueState other) {
      if (element == null) {
        return other;
      } else if (other.element == null) {
        return this;
      }
      duplicate = true;
      return this;
    }

    Object finish() {
      if (!duplicate) {
        return element;
      }
      return null;
    }
  }
}
