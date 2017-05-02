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

package tech.darkespresso.hellbinder;

/**
 * An interface to obtain a {@link CursorIterable} of {@link Entity Entities}. This is overridden by
 * generated code.
 *
 * @param <Entity> A class (annotated with {@link
 *     tech.darkespresso.hellbinder.annotations.ContentProviderEntity}.
 * @param <ContentResolver> The class representing Android's content resolver.
 */
public interface QueryExecutor<Entity, ContentResolver> {
  /**
   * Returns an iterable wrapped around a closeable resource, that can be used to obtain all of the
   * elements that satisfy the constraints of the query.
   *
   * @param contentResolver an object of type {@link ContentResolver} that will be used when
   *     retrieving the entity.
   * @return an iterable of objects that satisfy the query, or {@code null}.
   */
  CursorIterable<Entity> get(ContentResolver contentResolver);

  /**
   * Returns the first element that satisfies the query, or {@code null} if no such element exists.
   *
   * @param contentResolver an object of type {@link ContentResolver} that will be used when
   *     retrieving the entity.
   * @return the first object that satisfies the query, or {@code null}.
   */
  Entity getFirst(ContentResolver contentResolver);
}
