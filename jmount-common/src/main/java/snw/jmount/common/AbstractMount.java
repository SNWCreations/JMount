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

package snw.jmount.common;

import snw.jmount.Mount;
import snw.jmount.NameTransformer;
import snw.jmount.common.handle.ConstructorMPImpl;
import snw.jmount.common.handle.MethodHandleBasedFieldAccessor;
import snw.jmount.common.handle.MethodHandleBasedWrappedConstructor;
import snw.jmount.handle.ConstructorMP;
import snw.jmount.handle.FieldAccessor;
import snw.jmount.handle.WrappedConstructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;

import static snw.jmount.common.util.MountUtils.*;
import static snw.jmount.common.util.MountUtils.convertMethod;
import static snw.jmount.common.util.ReflectUtils.lookUpConstructor;
import static snw.jmount.common.util.ReflectUtils.lookUpField;

/**
 * A shared {@link snw.jmount.Mount} implementation with partly completed feature.
 *
 * @author SNWCreations
 * @since 0.1.0
 */
public abstract class AbstractMount implements Mount {
    protected final NameTransformer nameTransformer;
    protected final ClassLoader classLoader;

    public AbstractMount(AbstractMountBuilder builder) {
        this.nameTransformer = builder.nameTransformer;
        this.classLoader = builder.classLoader;
    }

    @Override
    public NameTransformer nameTransformer() {
        return nameTransformer;
    }

    @Override
    public ClassLoader classLoader() {
        return classLoader;
    }

    @Override
    public Class<?> findOriginClass(Class<?> mp) throws IllegalArgumentException {
        return convertToUnderlyingClass(mp, this);
    }

    @Override
    public Constructor<?> findConstructor(Class<?> originClazz, Class<?>... argTypes) throws NoSuchElementException {
        return lookUpConstructor(originClazz, argTypes, this);
    }

    @Override
    public <T> ConstructorMP<T> findConstructorAndWrapAsMP(Class<T> mp, Class<?>... argTypes) throws NoSuchElementException, IllegalArgumentException {
        return new ConstructorMPImpl<>(this, mp, findConstructorAndWrap(findOriginClass(mp), argTypes));
    }

    /**
     * Check if the class is a valid Mount Point and do nothing, otherwise this method fails.
     *
     * @param mp A Mount Point interface class
     */
    protected void verify(Class<?> mp) {
        Class<?> underlyingClass = findOriginClass(mp);
        for (Method m : mp.getDeclaredMethods()) {
            if (isFieldAccessor(m)) {
                checkIfIsFieldAccessor(m, this);
            } else {
                try {
                    convertMethod(m, this);
                } catch (NoSuchElementException e) {
                    if (!m.isDefault()) {
                        throw e; // rethrow
                    }
                }
            }
        }
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
        final Constructor<?> constructor = findConstructor(originClazz, argTypes);
        return new MethodHandleBasedWrappedConstructor(this, constructor);
    }

}
