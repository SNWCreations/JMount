/*
 * Copyright 2023 JMount contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package snw.jmount;

import org.jetbrains.annotations.Contract;
import snw.jmount.handle.ConstructorMP;
import snw.jmount.handle.FieldAccessor;
import snw.jmount.handle.WrappedConstructor;

import java.lang.reflect.Constructor;
import java.util.NoSuchElementException;

/**
 * An utility tool which is configured, ready-to-use for creating instances of Mount Points and origin things.
 *
 * @author SNWCreations
 * @since 0.1.0
 */
public interface Mount {

    /**
     * Create a Mount Point on the origin object.
     *
     * @param mpType The Mount Point interface class object
     * @param origin The origin object
     * @return The Mount Point instance
     * @param <T> The Mount Point type
     * @throws IllegalArgumentException Thrown if the origin is a Mount Point object,
     *                                  or {@code mpType} is not a valid Mount Point type.
     */
    <T> T mount(Class<T> mpType, Object origin) throws IllegalArgumentException;

    /**
     * Get the origin object from the Mount Point instance.
     *
     * @param mp The Mount Point object
     * @return The origin object
     * @throws IllegalArgumentException Thrown if the {@code mp} is not a Mount Point object.
     */
    Object unmount(Object mp) throws IllegalArgumentException;

    /**
     * Find the underlying class of the provided Mount Point type.
     *
     * @param mp The class object of the Mount Point type
     * @return The underlying class
     * @throws IllegalArgumentException Thrown if the provided class is not a Mount Point.
     */
    Class<?> findOriginClass(Class<?> mp) throws IllegalArgumentException;

    // -- Field --

    /**
     * Get an accessor of the provided static field.
     *
     * @param originClazz The origin class
     * @param fieldName The field name
     * @return The accessor
     * @throws NoSuchElementException Thrown if the requested field is not found
     */
    @Contract("_, _ -> new")
    FieldAccessor<?> accessStaticField(Class<?> originClazz, String fieldName) throws NoSuchElementException;

    /**
     * Get an accessor of the provided static field.
     *
     * @param originClazz The origin class
     * @param fieldName The field name
     * @param mountType The class object of the Mount Point type
     * @param <T> The Mount Point type, need to be compatible with the type of the underlying field
     * @return The accessor
     * @throws NoSuchElementException Thrown if the requested field is not found
     * @throws IllegalArgumentException Thrown if the T type is NOT compatible with the type of the underlying field
     */
    @Contract("_, _, _ -> new")
    <T> FieldAccessor<T> accessStaticField(Class<?> originClazz, String fieldName, Class<T> mountType)
            throws NoSuchElementException, IllegalArgumentException;

    /**
     * Get an accessor of the provided field on the provided object.
     *
     * @param origin The origin object
     * @param fieldName The field name
     * @return The accessor
     * @throws NoSuchElementException Thrown if the requested field is not found
     */
    @Contract("_, _ -> new")
    FieldAccessor<?> accessField(Object origin, String fieldName) throws NoSuchElementException;

    /**
     * Get an accessor of the provided field on the provided object.
     *
     * @param origin The origin object
     * @param fieldName The field name
     * @param mountType The class object of the Mount Point type
     * @param <T> The Mount Point type, need to be compatible with the type of the underlying field
     * @return The accessor
     * @throws NoSuchElementException Thrown if the requested field is not found
     * @throws IllegalArgumentException Thrown if the T type is NOT compatible with the type of the underlying field
     */
    @Contract("_, _, _ -> new")
    <T> FieldAccessor<T> accessField(Object origin, String fieldName, Class<T> mountType)
            throws NoSuchElementException, IllegalArgumentException;

    // -- Constructor --

    /**
     * Look up the constructor on the provided origin class with the provided argument types.
     *
     * @param originClazz The origin class
     * @param argTypes The argument types, Mount Points will be unmounted if needed
     * @return The constructor
     * @throws NoSuchElementException Thrown if the requested constructor is NOT found
     */
    Constructor<?> findConstructor(Class<?> originClazz, Class<?>... argTypes) throws NoSuchElementException;

    /**
     * Look up the constructor on the provided origin class with the provided argument types, and wrap it into
     *  the {@link WrappedConstructor}.
     *
     * @param originClazz The origin class
     * @param argTypes The argument types, Mount Points will be unmounted if needed
     * @return The constructor
     * @throws NoSuchElementException Thrown if the requested constructor is NOT found
     */
    @Contract("_, _ -> new")
    WrappedConstructor findConstructorAndWrap(Class<?> originClazz, Class<?>... argTypes) throws NoSuchElementException;

    /**
     * Look up the constructor on the provided origin class with the provided argument types, and wrap it into
     *  the {@link ConstructorMP}.
     *
     * @param mp The Mount Point interface class
     * @param argTypes The argument types, Mount Points will be unmounted if needed
     * @param <T> The Mount Point type
     * @return The constructor
     * @throws NoSuchElementException Thrown if the requested constructor is NOT found
     */
    @Contract("_, _ -> new")
    <T> ConstructorMP<T> findConstructorAndWrapAsMP(Class<T> mp, Class<?>... argTypes)
        throws NoSuchElementException, IllegalArgumentException;

    // -- Attributes --

    /**
     * Get the name transformer of this object.
     *
     * @return The name transformer
     */
    NameTransformer nameTransformer();

    /**
     * Get the class loader of this object.
     *
     * @return The class loader
     */
    ClassLoader classLoader();

}
