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

import java.util.List;
import tech.darkespresso.hellbinder.annotations.Column;
import tech.darkespresso.hellbinder.annotations.ContentProviderEntity;

/**
 * An object that wraps a cursor, can be used in a try-with-resource statement and is iterable. Its
 * iterator's {@link java.util.Iterator#next() next()} method automatically allocates and returns an
 * object of type {@link E}.
 *
 * @param <E> Any class annotated with {@link ContentProviderEntity ContentProviderEntity}
 */
public interface CloseableList<E> extends AutoCloseable, List<E> {
  /**
   * Returns the entity at the specified position.
   *
   * <p>If the second parameter is not null, its {@link Column}-annotated fields will be modified
   * accordingly, and it will be returned. Otherwise, a new instance of {@link E} will be allocated.
   *
   * @param index
   * @param e
   * @return an instance of {@link E} corresponding to the index-th row returned by the query that
   *     produced this list.
   */
  E get(int index, E e);

  @Override
  void close();
}
