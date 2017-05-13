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
 *
 * <p>For example, suppose that a class name {@code com.foobar.Contact} is annotated with {@code
 * @literal @ContentProviderEntity("Contacts")}:
 *
 * <pre>{@code
 * package com.foobar;
 * ...
 *@literal @ContentProivderEntity("Contacts")
 * public class Contact {
 *    @literal @ContentUri public static final Uri URI = ContactsContract.Contacts.CONTENT_URI;
 *
 *    @literal @Id
 *    @literal @Column(ContactsContract.Contacts._ID)
 *     public long id;
 *
 *    @literal @SortCriterion
 *    @literal @Column(ContactsContract.Contacts.DISPLAY_NAME)
 *     public String name;
 *
 *    @literal @Constraint
 *    @literal @Column(ContactsContract.Contacts.HAS_PHONE_NUMBER)
 *     public int hasPhoneNumber;
 *
 *    @literal @Column(ContactsContract.Contacts.PHOTO_ID)
 *     public String photoId;
 *     ...
 * }}</pre>
 *
 * The generated class will be:
 *
 * <pre>{@code
 * package com.foobar;
 * ...
 * public class Contacts {
 *   ...
 *   public static QueryBuilder where() { ... }
 *
 *   public static OrderBuilder sortBy() { ... }
 *
 *   public static CloseableList<Contact> get(ContentResolver contentResolver) { ... }
 *
 *   public static Contact getById(ContentResolver contentResolver, long id) { ... }
 * }}</pre>
 *
 * <p>The generated class will also contain a bunch of interfaces used to build queries. It is
 * then possible to write, for example:
 *
 * <pre>{@code
 *   ...
 *   CloseableList<Contact> contacts;
 *   try {
 *       contacts = Contacts.where()
 *           .hasPhoneNumber(Operator.EQ, 1)
 *           .sortBy()
 *           .name(Order.ASCENDING)
 *           .get(contentResolver);
 *       // do stuff with contacts
 *   } finally {
 *       contacts.close();
 *   }
 * }</pre>
 *
 * <p>Note that {@code where()} is only present if at least one field annotated with
 * {@link tech.darkespresso.hellbinder.annotations.Column Column} is also annotated with
 * {@link tech.darkespresso.hellbinder.annotations.Constraint Constraint}, {@code sortBy()} is
 * only present if at least one field annotated with
 * {@link tech.darkespresso.hellbinder.annotations.Column Column} is also annotated with
 * {@link tech.darkespresso.hellbinder.annotations.SortCriterion SortCriterion}, and {@code
 * getById()} appears only if there is one (and only one) field annotated with
 * {@link tech.darkespresso.hellbinder.annotations.Column Column} that is also annotated with
 * {@link tech.darkespresso.hellbinder.annotations.Id Id}.
 *
 * <p>{@code @ContentUri} can also be used to annotate a static method that requires some
 * parameters. In that case, the generated class will only contain a method called {@code
 * withUriParams(...)}, which takes the same parameters as the annotated method, and returns an
 * interface that expose the same methods that would otherwise be static.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ContentProviderEntity {
  /** The name of the generated class. */
  String value();
}
