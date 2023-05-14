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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import static snw.jmount.common.util.MountUtils.convertOrReturn;
import static snw.jmount.common.util.ReflectUtils.perform;

public class MethodHandleBasedFieldAccessor<T> extends AbstractFieldAccessor<T> {
    protected final MethodHandle getter;
    protected final MethodHandle setter;

    public MethodHandleBasedFieldAccessor(
            Mount mount,
            @Nullable Object underlyingObject,
            @Nullable Class<T> mountType,
            Field field
    ) {
        super(mount, underlyingObject, mountType, field);
        field.setAccessible(true);
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        this.getter = bindIfNeeded(perform(() -> lookup.unreflectGetter(field)));
        if (!isFinal()) {
            this.setter = bindIfNeeded(perform(() -> lookup.unreflectSetter(field)));
        } else {
            this.setter = null;
        }
    }

    protected final MethodHandle bindIfNeeded(MethodHandle handle) {
        if (underlyingObject != null) {
            return handle.bindTo(underlyingObject);
        }
        return handle;
    }

    @Override
    public Object get() {
        return perform(getter::invoke);
    }

    @Override
    protected T getMounted0() {
        // mountType should be not null at this time!
        // noinspection DataFlowIssue, unchecked
        return !mountType.isPrimitive() ? mount.mount(mountType, get()) : (T) get();
    }

    @Override
    protected void set0(Object newValue) {
        perform(() -> {
            final Object actualNewValue = convertOrReturn(newValue, mount);
            if (underlyingObject == null) {
                setter.invoke(null, actualNewValue);
            } else {
                setter.invoke(actualNewValue);
            }
            return null;
        });
    }
}
