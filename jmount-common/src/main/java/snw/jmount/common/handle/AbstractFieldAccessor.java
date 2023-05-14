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

package snw.jmount.common.handle;

import org.jetbrains.annotations.Nullable;
import snw.jmount.Mount;
import snw.jmount.handle.FieldAccessor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static snw.jmount.common.util.MountUtils.convertOrReturn;

/**
 * A shared implementation of {@link FieldAccessor} with partly completed feature.
 *
 * @param <T> The Mount Point type of the underlying type
 * @author SNWCreations
 * @since 0.1.0
 */
public abstract class AbstractFieldAccessor<T> implements FieldAccessor<T> {
    protected final Mount mount;
    protected final @Nullable Object underlyingObject;
    protected final @Nullable Class<T> mountType;
    protected final Field field;

    protected AbstractFieldAccessor(
            Mount mount,
            @Nullable Object underlyingObject,
            @Nullable Class<T> mountType,
            Field field
    ) {
        if (!field.getType().isAssignableFrom(convertOrReturn(mountType, mount))) {
            throw new IllegalArgumentException(
                    "The type of the field is not compatible with the provided Mount Point type"
            );
        }
        this.mount = mount;
        this.underlyingObject = underlyingObject;
        this.mountType = mountType;
        this.field = field;
    }

    @Override
    public void set(Object newValue) throws IllegalArgumentException {
        if (isFinal()) {
            throw new IllegalStateException("The underlying field is final");
        }
        if (newValue != null && !field.getType().isAssignableFrom(convertOrReturn(newValue.getClass(), mount))) {
            throw new IllegalArgumentException("The type of the new value is not compatible with the underlying field");
        }
        set0(newValue);
    }

    @Override
    public T getMounted() throws IllegalStateException {
        if (this.mountType == null) {
            throw new IllegalStateException("This accessor cannot create mounted objects because target type is unknown");
        }
        return getMounted0();
    }

    protected abstract T getMounted0();

    // Convert the newValue into origin object if needed
    protected abstract void set0(Object newValue);

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(field.getModifiers());
    }

    @Override
    public Field getUnderlyingField() {
        return field;
    }
}
