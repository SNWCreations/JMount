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

package snw.jmount.impl.jdk;

import snw.jmount.common.AbstractMount;
import snw.jmount.common.AbstractMountBuilder;
import snw.jmount.common.handle.MethodHandleBasedFieldAccessor;
import snw.jmount.common.handle.MethodHandleBasedWrappedConstructor;
import snw.jmount.handle.FieldAccessor;
import snw.jmount.handle.WrappedConstructor;
import snw.jmount.impl.jdk.handle.MountInvocationHandler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.NoSuchElementException;

import static snw.jmount.common.util.MountUtils.convertToUnderlyingClass;
import static snw.jmount.common.util.ReflectUtils.lookUpConstructor;
import static snw.jmount.common.util.ReflectUtils.lookUpField;

/**
 * The {@link snw.jmount.Mount} implementation based on pure JDK things.
 *
 * @author SNWCreations
 * @since 0.1.0
 */
public class JDKMount extends AbstractMount {

    public JDKMount(AbstractMountBuilder builder) {
        super(builder);
    }

    @Override
    public <T> T mount(Class<T> mpType, Object origin) throws IllegalArgumentException {
        if (!origin.getClass().isAssignableFrom(convertToUnderlyingClass(mpType, this))) {
            throw new IllegalArgumentException("The target Mount Point type is not compatible with the provided object");
        }
        verify(mpType);
        // noinspection unchecked
        return (T) Proxy.newProxyInstance(
                classLoader(), new Class[]{mpType}, new MountInvocationHandler(this, origin)
        );
    }

    @Override
    public Object unmount(Object mp) throws IllegalArgumentException {
        return ((MountInvocationHandler) Proxy.getInvocationHandler(mp)).getOrigin();
    }

    @Override
    public FieldAccessor<?> accessStaticField(Class<?> originClazz, String fieldName) throws NoSuchElementException {
        return accessStaticField(originClazz, fieldName, null);
    }

    @Override
    public <T> FieldAccessor<T> accessStaticField(Class<?> originClazz, String fieldName, Class<T> mountType) throws NoSuchElementException, IllegalArgumentException {
        final Field underlyingField = lookUpField(originClazz, fieldName, this);
        return new MethodHandleBasedFieldAccessor<>(this, null, mountType, underlyingField);
    }

    @Override
    public FieldAccessor<?> accessField(Object origin, String fieldName) throws NoSuchElementException {
        return accessField(origin, fieldName, null);
    }

    @Override
    public <T> FieldAccessor<T> accessField(Object origin, String fieldName, Class<T> mountType) throws NoSuchElementException, IllegalArgumentException {
        final Field underlyingField = lookUpField(origin.getClass(), fieldName, this);
        return new MethodHandleBasedFieldAccessor<>(this, null, mountType, underlyingField);
    }

    @Override
    public WrappedConstructor findConstructorAndWrap(Class<?> originClazz, Class<?>... argTypes) throws NoSuchElementException {
        final Constructor<?> constructor = lookUpConstructor(originClazz, argTypes, this);
        return new MethodHandleBasedWrappedConstructor(constructor);
    }
}
