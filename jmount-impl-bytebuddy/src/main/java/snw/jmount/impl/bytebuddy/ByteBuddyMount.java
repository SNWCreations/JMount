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

package snw.jmount.impl.bytebuddy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import snw.jmount.common.AbstractMount;
import snw.jmount.common.AbstractMountBuilder;
import snw.jmount.common.exceptions.ReflectOperationException;
import snw.jmount.impl.bytebuddy.handle.ByteBuddyInterceptor;
import snw.jmount.impl.bytebuddy.util.ClassDumper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.not;
import static snw.jmount.common.util.ReflectUtils.perform;

/**
 * The ByteBuddy-based {@link snw.jmount.Mount} implementation.
 *
 * @author SNWCreations
 * @since 0.1.0
 */
// TODO Make this pass the test
public class ByteBuddyMount extends AbstractMount {
    protected static final String UNDERLYING_OBJECT_FIELD_NAME = "__BYTEBUDDY_UNDERLYING_OBJECT__";
    protected final Map<Class<?>, Class<?>> proxyTypeMap = new ConcurrentHashMap<>();

    public ByteBuddyMount(AbstractMountBuilder builder) {
        super(builder);
    }

    @Override
    public <T> T mount(Class<T> mpType, Object origin) throws IllegalArgumentException {
        if (!findOriginClass(mpType).isAssignableFrom(origin.getClass())) {
            throw new IllegalArgumentException("The target Mount Point type is not compatible with the provided object");
        }
        verify(mpType);
        final Class<?> loaded = proxyTypeMap.computeIfAbsent(mpType, i -> generateProxyClass(i, origin.getClass()));
        // noinspection unchecked
        return (T) perform(() -> loaded.getDeclaredConstructor(origin.getClass()).newInstance(origin));
    }

    protected Class<?> generateProxyClass(Class<?> mpType, Class<?> originType) {
        final DynamicType.Unloaded<Object> unloaded = new ByteBuddy()
                .subclass(Object.class)
                .implement(mpType)
                .defineField(UNDERLYING_OBJECT_FIELD_NAME, originType, Visibility.PUBLIC, FieldManifestation.FINAL)
                .method(isDeclaredBy(mpType).and(not(isDeclaredBy(originType))))
                .intercept(MethodDelegation.to(new ByteBuddyInterceptor(this, new AtomicReference<>())))
                .defineConstructor(Visibility.PUBLIC)
                .withParameters(originType)
                .intercept(
                        MethodCall.invoke(perform(() -> Object.class.getDeclaredConstructor()))
                                .andThen(FieldAccessor.ofField(UNDERLYING_OBJECT_FIELD_NAME).setsArgumentAt(0))
                )
                .make();
        ClassDumper.DUMPER.accept(unloaded); // Dump will be performed if needed
        return unloaded.load(classLoader(), ClassLoadingStrategy.Default.WRAPPER).getLoaded();
    }

    @Override
    public Object unmount(Object mp) throws IllegalArgumentException {
        try {
            return perform(() -> mp.getClass().getDeclaredField(UNDERLYING_OBJECT_FIELD_NAME).get(mp));
        } catch (ReflectOperationException e) {
            if (e.getCause() instanceof NoSuchFieldException) {
                throw new IllegalArgumentException("Not a Mount Point");
            }
            throw e; // rethrow
        }
    }

    @Override
    public boolean isMount(Object mp) {
        try {
            mp.getClass().getDeclaredField(UNDERLYING_OBJECT_FIELD_NAME);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
}
