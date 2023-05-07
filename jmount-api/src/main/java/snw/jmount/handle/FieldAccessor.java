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

package snw.jmount.handle;


import java.lang.reflect.Field;

/**
 * The field accessor, provides the ability that wrapping the underlying data as the Mount Point object.
 *
 * @param <T> The Mount Point type
 * @author SNWCreations
 * @since 0.1.0
 */
public interface FieldAccessor<T> {

    /**
     * Get the origin object inside the underlying field.
     *
     * @return The origin object
     */
    Object get();

    /**
     * Set the provided value as the new value to the underlying field.
     *
     * @param newValue The new value
     * @throws IllegalArgumentException Thrown if the type of the new value does NOT match
     *                                  the type of the underlying field
     */
    void set(Object newValue) throws IllegalArgumentException;

    /**
     * Get the wrapped Mount Point object of the data inside the underlying field.
     *
     * @return The Mount Point object
     * @throws IllegalStateException Thrown if the exact Mount Point type is unknown for this accessor instance
     */
    T getMounted() throws IllegalStateException;

    /**
     * Return true if the underlying field is marked as final.
     *
     * @return The field final status
     */
    boolean isFinal();

    /**
     * Get the underlying field.
     *
     * @return The underlying field
     */
    Field getUnderlyingField();

}
