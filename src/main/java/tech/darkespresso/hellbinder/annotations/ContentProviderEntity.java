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
package tech.darkespresso.hellbinder.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Annotates a class with fields that should be mapped to columns of a Cursor returned by a query
 * to a ContentProvider.
 * <p>The hellbinder annotation processor will generate a class containing static utility methods
 * to retrieve objects of this class.
 * <p>For example:
 * <pre>{@code
 *     package foo.bar;
 *
 *    @literal @ContentProviderEntity("ContactsCollection")
 *     public class Contact {
 *        @literal @ContentUri public static final Uri URI = Contacts.CONTENT_URI;
 *        @literal @Id @Column(Contacts._ID) public long id;
 *         ...
 *     }
 * }
 * <p>will generate:
 * <pre>{@code
 *     package foo.bar;
 *
 *     public final class ContactsCollection {
 *         ...
 *         public static CursorIterable<Contact> get(ContentProvider contentProvider) {...}
 *         public static Contact getById(ContentProvider contentProvider, long id) {...}
 *         ...
 *     }
 * }
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ContentProviderEntity {
  /** The name of the generated class. */
  String value();
}
