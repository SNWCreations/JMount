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
import snw.jmount.handle.ConstructorMP;

import java.lang.reflect.Constructor;
import java.util.NoSuchElementException;

import static snw.jmount.common.util.MountUtils.convertToUnderlyingClass;
import static snw.jmount.common.util.ReflectUtils.lookUpConstructor;

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
     * Check if the class is a valid Mount Point.
     *
     * @param mp A Mount Point interface class
     */
    protected abstract void verify(Class<?> mp);
}
